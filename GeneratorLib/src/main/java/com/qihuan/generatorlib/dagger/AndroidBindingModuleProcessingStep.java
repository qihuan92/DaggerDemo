package com.qihuan.generatorlib.dagger;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.qihuan.annotationlib.dagger.AutoBinding;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

import static com.qihuan.generatorlib.dagger.Const.DAGGER_GEN_PACKAGE;

public class AndroidBindingModuleProcessingStep implements BasicAnnotationProcessor.ProcessingStep {

    private Filer filer;

    public AndroidBindingModuleProcessingStep(Filer filer) {
        this.filer = filer;
    }

    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
        return ImmutableSet.of(
                AutoBinding.class
        );
    }

    @Override
    public Set<? extends Element> process(SetMultimap<Class<? extends Annotation>, Element> setMultimap) {
        List<Element> elementList = new ArrayList<>(setMultimap.values());
        if (elementList.size() != 0) {
            generate(elementList);
        }
        return ImmutableSet.of();
    }

    private void generate(List<Element> elementList) {
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
        JavaFile javaFile = JavaFile.builder(
                DAGGER_GEN_PACKAGE,
                TypeSpec.classBuilder("AndroidBindingModule")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addAnnotation(Module.class)
                        .addMethods(methodSpecList)
                        .build()
        ).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
