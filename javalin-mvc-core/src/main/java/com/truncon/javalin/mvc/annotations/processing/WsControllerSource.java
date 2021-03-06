package com.truncon.javalin.mvc.annotations.processing;

import com.squareup.javapoet.CodeBlock;
import com.truncon.javalin.mvc.api.ws.*;
import com.truncon.javalin.mvc.ws.*;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class WsControllerSource {
    private final Types typeUtils;
    private final Elements elementUtils;
    private final TypeElement controllerElement;

    private WsControllerSource(Types typeUtils, Elements elementUtils, TypeElement controllerElement) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.controllerElement = controllerElement;
    }

    public static List<WsControllerSource> getWsControllers(Types typeUtils, Elements elementUtils, RoundEnvironment environment) throws ProcessingException {
        Set<? extends Element> controllerElements = environment.getElementsAnnotatedWith(WsController.class);
        checkControllerElements(controllerElements);
        return controllerElements.stream()
            .map(e -> (TypeElement)e)
            .map(e -> new WsControllerSource(typeUtils, elementUtils, e))
            .collect(Collectors.toList());
    }

    private static void checkControllerElements(Set<? extends Element> elements) throws ProcessingException {
        Element[] badElements = elements.stream()
            .filter(e -> e.getKind() != ElementKind.CLASS)
            .toArray(Element[]::new);
        if (badElements.length > 0) {
            throw new ProcessingException("WsController annotations can only be applied to classes.", badElements);
        }
    }

    public CodeBlock generateEndpoint(ContainerSource container, String app) throws ProcessingException {
        ExecutableElement connectMethod = getAnnotatedMethod(WsConnect.class);
        ExecutableElement disconnectMethod = getAnnotatedMethod(WsDisconnect.class);
        ExecutableElement errorMethod = getAnnotatedMethod(WsError.class);
        ExecutableElement messageMethod = getAnnotatedMethod(WsMessage.class);
        ExecutableElement binaryMessageMethod = getAnnotatedMethod(WsBinaryMessage.class);
        if (connectMethod == null
            && disconnectMethod == null
            && errorMethod == null
            && messageMethod == null
            && binaryMessageMethod == null) {
            return null;
        }

        CodeBlock.Builder handlerBuilder = CodeBlock.builder();
        handlerBuilder.beginControlFlow("$N.ws($S, (ws) ->", app, getRoute());

        addOnConnectHandler(container, handlerBuilder, connectMethod);
        addOnDisconnectHandler(container, handlerBuilder, disconnectMethod);
        addOnErrorHandler(container, handlerBuilder, errorMethod);
        addOnMessageHandler(container, handlerBuilder, messageMethod);
        addOnBinaryMessageHandler(container, handlerBuilder, binaryMessageMethod);

        handlerBuilder.endControlFlow(")");
        return handlerBuilder.build();
    }

    private <A extends Annotation> ExecutableElement getAnnotatedMethod(Class<A> annotationType) throws ProcessingException {
        List<ExecutableElement> methods = controllerElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.METHOD)
            .filter(e -> e.getAnnotation(annotationType) != null)
            .map(e -> (ExecutableElement)e)
            .collect(Collectors.toList());
        if (methods.isEmpty()) {
            return null;
        } else if (methods.size() > 1) {
            String message = "Only a single method can be annotated with the "
                + annotationType.getCanonicalName()
                + " annotation.";
            throw new ProcessingException(message, controllerElement);
        } else {
            return methods.get(0);
        }
    }

    private String getRoute() {
        WsController route = controllerElement.getAnnotation(WsController.class);
        return route.route();
    }

    private void addOnConnectHandler(
            ContainerSource container,
            CodeBlock.Builder handlerBuilder,
            ExecutableElement method) {
        addHandler(
            container,
            handlerBuilder,
            "onConnect",
            WsConnectContext.class,
            JavalinWsConnectContext.class,
            method);
    }

    private void addOnDisconnectHandler(
            ContainerSource container,
            CodeBlock.Builder handlerBuilder,
            ExecutableElement method) {
        addHandler(
            container,
            handlerBuilder,
            "onClose",
            WsDisconnectContext.class,
            JavalinWsDisconnectContext.class,
            method);
    }

    private void addOnErrorHandler(
            ContainerSource container,
            CodeBlock.Builder handlerBuilder,
            ExecutableElement method) {
        addHandler(
            container,
            handlerBuilder,
            "onError",
            WsErrorContext.class,
            JavalinWsErrorContext.class,
            method);
    }

    private void addOnMessageHandler(
            ContainerSource container,
            CodeBlock.Builder handlerBuilder,
            ExecutableElement method) {
        addHandler(
            container,
            handlerBuilder,
            "onMessage",
            WsMessageContext.class,
            JavalinWsMessageContext.class,
            method);
    }

    private void addOnBinaryMessageHandler(
            ContainerSource container,
            CodeBlock.Builder handlerBuilder,
            ExecutableElement method) {
        addHandler(
            container,
            handlerBuilder,
            "onBinaryMessage",
            WsBinaryMessageContext.class,
            JavalinWsBinaryMessageContext.class,
            method);
    }

    private void addHandler(
            ContainerSource container,
            CodeBlock.Builder handlerBuilder,
            String javalinHandler,
            Class<?> contextInterface,
            Class<?> contextImpl,
            ExecutableElement method) {
        if (method == null) {
            return;
        }
        final String context = "ctx";
        handlerBuilder.beginControlFlow("ws.$N(($N) ->", javalinHandler, context);
        final String wrapper = "context";
        handlerBuilder.addStatement("$T $N = new $T($N)", contextInterface, wrapper, contextImpl, context);
        addController(container, handlerBuilder);
        if (ParameterGenerator.isWsBinderNeeded(typeUtils, elementUtils, method, contextInterface)) {
            handlerBuilder.addStatement(
                "$T binder = new $T($N)",
                WsModelBinder.class,
                DefaultWsModelBinder.class,
                wrapper);
        }
        String parameters = ParameterGenerator.bindWsParameters(
                typeUtils,
                elementUtils,
                method,
                context,
                contextInterface,
                wrapper);
        MethodUtils methodUtils = new MethodUtils(typeUtils, elementUtils);
        if (methodUtils.hasVoidReturnType(method)) {
            handlerBuilder.addStatement("controller.$N(" + parameters + ")", method.getSimpleName());
        } else if (methodUtils.hasWsActionResultReturnType(method)) {
            handlerBuilder.addStatement(
                "$T result = controller.$N(" + parameters + ")",
                WsActionResult.class,
                method.getSimpleName());
            handlerBuilder.addStatement("result.execute($N)", wrapper);
        } else if (methodUtils.hasFutureWsActionResultReturnType(method)) {
            handlerBuilder.addStatement(
                "controller.$N(" + parameters + ").thenApply(r -> r.execute($N))",
                method.getSimpleName(),
                wrapper);
        } else if (methodUtils.hasFutureSimpleReturnType(method)) {
            handlerBuilder.addStatement(
                "controller.$N(" + parameters + ").thenApply(p -> new $T(p).execute($N))",
                method.getSimpleName(),
                WsJsonResult.class,
                wrapper);
        } else {
            handlerBuilder.addStatement(
                "$T result = controller.$N(" + parameters + ")",
                method.getReturnType(),
                method.getSimpleName());
            handlerBuilder.addStatement("new $T(result).execute($N)", WsJsonResult.class, wrapper);
        }
        handlerBuilder.endControlFlow(")");
    }

    private void addController(ContainerSource container, CodeBlock.Builder handlerBuilder) {
        Name controllerName = container.getDependencyName(controllerElement);
        if (container.isFound() && controllerName != null) {
            handlerBuilder.addStatement("$T injector = scopeFactory.get()", container.getType());
            handlerBuilder.addStatement("$T controller = injector.$L()", controllerElement, controllerName);
        } else {
            handlerBuilder.addStatement("$T controller = new $T()", controllerElement, controllerElement);
        }
    }
}
