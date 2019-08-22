package com.qihuan.generatorlib.dagger;

import com.google.auto.service.AutoService;
import com.qihuan.annotationlib.dagger.AutoAppBinding;
import com.qihuan.annotationlib.dagger.AutoModule;
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
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.android.AndroidInjector;

/**
 * AutoAppComponentProcessor
 *
 * @author qi
 * @date 2019-08-21
 */
@SuppressWarnings("unused")
@AutoService(Processor.class)
public class AutoAppComponentProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;

    private String packageName;
    private Element autoAppBindingElement;
    private List<Element> autoModuleElementList;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elementUtils = processingEnvironment.getElementUtils();

        autoModuleElementList = new ArrayList<>();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> annotations = new LinkedHashSet<>();
        annotations.add(AutoAppBinding.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        autoAppBindingElement = roundEnvironment.getElementsAnnotatedWith(AutoAppBinding.class).stream().findFirst().orElse(null);
        autoModuleElementList.addAll(roundEnvironment.getElementsAnnotatedWith(AutoModule.class));
        genCode();
        return false;
    }

    private void genCode() {
        if (autoAppBindingElement == null) {
            return;
        }
        packageName = elementUtils.getPackageOf(autoAppBindingElement).toString() + ".di";

        // app module
        JavaFile appModuleFile = appModule(autoAppBindingElement);
        try {
            appModuleFile.writeTo(filer);
        } catch (IOException e) {
            note(null, e.getMessage());
        }

        // app component
        JavaFile appComponentFile = appComponent(autoAppBindingElement);
        try {
            appComponentFile.writeTo(filer);
        } catch (IOException e) {
            note(null, e.getMessage());
        }
    }

    private JavaFile appModule(Element element) {
        if (element == null) {
            return null;
        }
        return JavaFile.builder(
                packageName,
                TypeSpec.classBuilder("AppModule")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addAnnotation(AutoModule.class)
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
        // Modules
        AnnotationSpec.Builder componentAnnotationBuilder = AnnotationSpec.builder(Component.class);
        componentAnnotationBuilder
//                .addMember("modules", "$T.class", ClassName.get(packageName, "AppModule"))
//                .addMember("modules", "$T.class", ClassName.get("com.qihuan.dagger", "AndroidBindingModule"))
                .addMember("modules", "$T.class", ClassName.get("dagger.android.support", "AndroidSupportInjectionModule"));
        for (Element moduleElement : autoModuleElementList) {
            note(moduleElement, "add");
            componentAnnotationBuilder.addMember("modules", "$T.class", ClassName.get(moduleElement.asType()));
        }

        return JavaFile.builder(
                packageName,
                TypeSpec.interfaceBuilder("AppComponent")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Singleton.class)
                        .addAnnotation(componentAnnotationBuilder.build())
                        .addSuperinterface(ParameterizedTypeName.get(ClassName.get(AndroidInjector.class), ClassName.get(element.asType())))
                        .addType(
                                TypeSpec.interfaceBuilder("Builder")
                                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                        .addAnnotation(Component.Builder.class)
                                        .addMethod(
                                                MethodSpec.methodBuilder("application")
                                                        .addAnnotation(BindsInstance.class)
                                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                                        .addParameter(ClassName.get("android.app", "Application"), "application")
                                                        .returns(ClassName.get(packageName, "AppComponent.Builder"))
                                                        .build()
                                        )
                                        .addMethod(
                                                MethodSpec.methodBuilder("build")
                                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                                        .returns(ClassName.get(packageName, "AppComponent"))
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
