package com.qihuan.generatorlib.dagger;

import com.google.auto.service.AutoService;
import com.qihuan.annotationlib.dagger.AutoAppBinding;
import com.qihuan.annotationlib.dagger.AutoBinding;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;

/**
 * AutoComponentProcessor
 *
 * @author qi
 * @date 2019-08-21
 */
@SuppressWarnings("unused")
@AutoService(Processor.class)
public class AutoComponentProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private List<Element> autoBindingElementList;
    private Element autoAppBindingElement;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        autoBindingElementList = new ArrayList<>();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> annotations = new LinkedHashSet<>();
        annotations.add(AutoBinding.class.getCanonicalName());
        annotations.add(AutoAppBinding.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(AutoBinding.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "Only classes can be annotated with @%s", AutoBinding.class.getSimpleName());
                return true;
            }
            autoBindingElementList.add(element);
        }
        autoAppBindingElement = roundEnvironment.getElementsAnnotatedWith(AutoAppBinding.class).stream().findFirst().orElse(null);
        genCode();
        return false;
    }

    private void genCode() {
        // android binding
        JavaFile androidBindingFile = androidBindingFile(autoBindingElementList);

        // app module
        JavaFile appModuleFile = appModule(autoAppBindingElement);

        // app component
        JavaFile appComponentFile = appComponent(autoAppBindingElement);

        try {
            androidBindingFile.writeTo(filer);
            appModuleFile.writeTo(filer);
            appComponentFile.writeTo(filer);
        } catch (IOException ignored) {
        }
    }

    private JavaFile androidBindingFile(List<Element> elementList) {
        List<MethodSpec> methodSpecList = new ArrayList<>();
        for (Element element : elementList) {
            // 获取注解中的 module
            AutoBinding annotation = element.getAnnotation(AutoBinding.class);
            List<TypeMirror> moduleTypeMirrorList = new ArrayList<>();
            try {
                annotation.modules();
            } catch (MirroredTypesException e) {
                if (e.getTypeMirrors() != null) {
                    moduleTypeMirrorList.addAll(e.getTypeMirrors());
                }
            }
            AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(ContributesAndroidInjector.class);
            for (TypeMirror typeMirror : moduleTypeMirrorList) {
                annotationSpecBuilder.addMember("modules", "$T.class", ClassName.get(typeMirror));
            }
            AnnotationSpec annotationSpec = annotationSpecBuilder.build();
            // inject 函数
            methodSpecList.add(
                    MethodSpec.methodBuilder("inject" + element.getSimpleName().toString())
                            .addModifiers(Modifier.ABSTRACT)
                            .addAnnotation(annotationSpec)
                            .returns(TypeName.get(element.asType()))
                            .build()
            );
        }
        return JavaFile.builder(
                "com.qihuan.dagger",
                TypeSpec.classBuilder("AndroidBindingModule")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addAnnotation(Module.class)
                        .addMethods(methodSpecList)
                        .build()
        ).build();
    }

    private JavaFile appModule(Element element) {
        if (element == null) {
            return null;
        }
        return JavaFile.builder(
                "com.qihuan.dagger",
                TypeSpec.classBuilder("AppModule")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addAnnotation(Module.class)
                        .addMethod(
                                MethodSpec.methodBuilder("inject" + element.getSimpleName().toString())
                                        .addModifiers(Modifier.ABSTRACT)
                                        .addAnnotation(Binds.class)
                                        .addParameter(TypeName.get(element.asType()), element.getSimpleName().toString().toLowerCase())
                                        .returns(ClassName.get("android.app", "Application"))
                                        .build()
                        )
                        .build()
        ).build();
    }

    private JavaFile appComponent(Element element) {
        return JavaFile.builder(
                "com.qihuan.dagger",
                TypeSpec.interfaceBuilder("AppComponent")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Singleton.class)
                        .addAnnotation(
                                AnnotationSpec.builder(Component.class)
                                        .addMember("modules", "$T.class", ClassName.get("com.qihuan.dagger", "AppModule"))
                                        .addMember("modules", "$T.class", ClassName.get("com.qihuan.dagger", "AndroidBindingModule"))
                                        .addMember("modules", "$T.class", ClassName.get("dagger.android.support", "AndroidSupportInjectionModule"))
                                        .build()
                        )
                        .addSuperinterface(ParameterizedTypeName.get(ClassName.get(AndroidInjector.class), ClassName.get(element.asType())))
                        .addType(
                                TypeSpec.interfaceBuilder("Builder")
                                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                        .addAnnotation(Component.Builder.class)
                                        .addMethod(
                                                MethodSpec.methodBuilder("application")
                                                        .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                                                        .addParameter(ClassName.get("android.app", "Application"), "application")
                                                        .returns(ClassName.get("com.qihuan.dagger", "AppComponent.Builder"))
                                                        .build()
                                        )
                                        .addMethod(
                                                MethodSpec.methodBuilder("build")
                                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                                        .returns(ClassName.get("com.qihuan.dagger", "AppComponent"))
                                                        .build()
                                        )
                                        .build()
                        )
                        .build()
        ).build();
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    private void note(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args), e);
    }
}
