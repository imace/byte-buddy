package net.bytebuddy.instrumentation.method.bytecode.bind.annotation;

import net.bytebuddy.instrumentation.Instrumentation;
import net.bytebuddy.instrumentation.attribute.annotation.AnnotationDescription;
import net.bytebuddy.instrumentation.method.MethodDescription;
import net.bytebuddy.instrumentation.method.ParameterDescription;
import net.bytebuddy.instrumentation.method.bytecode.bind.MethodDelegationBinder;
import net.bytebuddy.instrumentation.method.bytecode.stack.assign.Assigner;
import net.bytebuddy.instrumentation.type.auxiliary.TypeProxy;

import java.lang.annotation.*;

/**
 * Parameters that are annotated with this annotation are assigned an instance of an auxiliary proxy type that allows calling
 * any default method of an interface of the instrumented type where the parameter type must be an interface that is
 * directly implemented by the instrumented type. The generated proxy will directly implement the parameter's
 * interface. If the interface of the annotation is not implemented by the instrumented type, the method with this
 * parameter is not considered as a binding target.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Default {

    /**
     * Determines if the generated proxy should be {@link java.io.Serializable}. If the annotated type
     * already is serializable, such an explicit specification is not required.
     *
     * @return {@code true} if the generated proxy should be {@link java.io.Serializable}.
     */
    boolean serializableProxy() default false;

    /**
     * A binder for the {@link net.bytebuddy.instrumentation.method.bytecode.bind.annotation.Default} annotation.
     */
    enum Binder implements TargetMethodAnnotationDrivenBinder.ParameterBinder<Default> {

        /**
         * The singleton instance.
         */
        INSTANCE;

        @Override
        public Class<Default> getHandledType() {
            return Default.class;
        }

        @Override
        public MethodDelegationBinder.ParameterBinding<?> bind(AnnotationDescription.Loadable<Default> annotation,
                                                               MethodDescription source,
                                                               ParameterDescription target,
                                                               Instrumentation.Target instrumentationTarget,
                                                               Assigner assigner) {
            if (!target.getTypeDescription().isInterface()) {
                throw new IllegalStateException(target + " uses the @Default annotation on a non-interface type");
            } else if (source.isStatic() || !instrumentationTarget.getTypeDescription().getInterfaces().contains(target.getTypeDescription())) {
                return MethodDelegationBinder.ParameterBinding.Illegal.INSTANCE;
            } else {
                return new MethodDelegationBinder.ParameterBinding.Anonymous(new TypeProxy.ForDefaultMethod(target.getTypeDescription(),
                        instrumentationTarget,
                        annotation.loadSilent().serializableProxy()));
            }
        }

        @Override
        public String toString() {
            return "Default.Binder." + name();
        }
    }
}

