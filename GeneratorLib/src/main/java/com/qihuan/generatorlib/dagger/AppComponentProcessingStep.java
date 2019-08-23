package com.qihuan.generatorlib.dagger;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.qihuan.annotationlib.dagger.AutoAppBinding;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.android.AndroidInjector;

import static com.qihuan.generatorlib.dagger.Const.COLLECTION_MODULE_NAME;
import static com.qihuan.generatorlib.dagger.Const.DAGGER_GEN_PACKAGE;

public class AppComponentProcessingStep implements BasicAnnotationProcessor.ProcessingStep {

    private Filer filer;
    private Elements elements;

    public AppComponentProcessingStep(Filer filer, Elements elements) {
        this.filer = filer;
        this.elements = elements;
    }

    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
        return ImmutableSet.of(
                AutoAppBinding.class
        );
    }

    @Override
    public Set<? extends Element> process(SetMultimap<Class<? extends Annotation>, Element> setMultimap) {
        setMultimap.forEach((annotationClass, element) -> {
            if (annotationClass.isAssignableFrom(AutoAppBinding.class)) {
                generate(element);
            }
        });
        return ImmutableSet.of();
    }

    private void generate(Element element) {
        String packageName = elements.getPackageOf(element).toString() + ".di";
        generateAppModule(element, packageName);
        generateAppComponent(element, packageName);
    }

    private void generateAppComponent(Element element, String packageName) {
        String componentName = element.getSimpleName().toString() + "Component";

        // Modules
        AnnotationSpec.Builder componentAnnotationBuilder = AnnotationSpec.builder(Component.class);
        componentAnnotationBuilder
                .addMember("modules", "$T.class", ClassName.get(packageName, "AppModule"))
                .addMember("modules", "$T.class", ClassName.get(DAGGER_GEN_PACKAGE, COLLECTION_MODULE_NAME))
                .addMember("modules", "$T.class", ClassName.get("dagger.android.support", "AndroidSupportInjectionModule"));

        JavaFile javaFile = JavaFile.builder(
                packageName,
                TypeSpec.interfaceBuilder(componentName)
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

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateAppModule(Element element, String packageName) {
        String moduleName = element.getSimpleName().toString() + "Module";
        JavaFile javaFile = JavaFile.builder(
                packageName,
                TypeSpec.classBuilder(moduleName)
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

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
