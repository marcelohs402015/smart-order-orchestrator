package com.marcelo.orchestrator.mcp.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Detector de design patterns e verificação SOLID.
 *
 * <p>Analisa código para identificar:
 * - Design Patterns (Factory, Adapter, Strategy, etc.)
 * - Conformidade com princípios SOLID</p>
 *
 * @author Marcelo Hernandes da Silva
 */
@Slf4j
@Component
public class PatternDetector {

    private final JavaParser javaParser;

    public PatternDetector() {
        this.javaParser = new JavaParser();
    }

    /**
     * Analisa código para detectar patterns e verificar SOLID.
     */
    public Map<String, Object> analyze(String code, boolean checkSolid) {
        Map<String, Object> result = new HashMap<>();
        List<String> patterns = new ArrayList<>();

        try {
            CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
                String className = clazz.getNameAsString();

                // Detectar Factory Pattern
                if (className.contains("Factory") && hasCreateMethod(clazz)) {
                    patterns.add("Factory Pattern");
                }

                // Detectar Adapter Pattern
                if (className.contains("Adapter") && implementsInterface(clazz)) {
                    patterns.add("Adapter Pattern");
                }

                // Detectar Strategy Pattern
                if (hasStrategyMethods(clazz)) {
                    patterns.add("Strategy Pattern");
                }
            });

            result.put("patterns", patterns);

            if (checkSolid) {
                result.put("solid", checkSolidPrinciples(code, cu));
            }

        } catch (Exception e) {
            log.error("Error detecting patterns", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    private boolean hasCreateMethod(ClassOrInterfaceDeclaration clazz) {
        return clazz.getMethods().stream()
            .anyMatch(m -> m.getNameAsString().equalsIgnoreCase("create"));
    }

    private boolean implementsInterface(ClassOrInterfaceDeclaration clazz) {
        return !clazz.getImplementedTypes().isEmpty();
    }

    private boolean hasStrategyMethods(ClassOrInterfaceDeclaration clazz) {
        return clazz.getMethods().stream()
            .anyMatch(m -> m.getNameAsString().contains("execute") || m.getNameAsString().contains("strategy"));
    }

    private Map<String, Boolean> checkSolidPrinciples(String code, CompilationUnit cu) {
        Map<String, Boolean> solid = new HashMap<>();

        // Verificação básica (pode ser expandida)
        solid.put("Single Responsibility", checkSingleResponsibility(cu));
        solid.put("Open/Closed", true); // Simplificado
        solid.put("Liskov Substitution", true); // Simplificado
        solid.put("Interface Segregation", checkInterfaceSegregation(cu));
        solid.put("Dependency Inversion", checkDependencyInversion(code));

        return solid;
    }

    private boolean checkSingleResponsibility(CompilationUnit cu) {
        // Verificar se a classe tem muitas responsabilidades
        return cu.findAll(ClassOrInterfaceDeclaration.class).stream()
            .allMatch(clazz -> clazz.getMethods().size() < 15);
    }

    private boolean checkInterfaceSegregation(CompilationUnit cu) {
        // Verificar se interfaces são pequenas e focadas
        return cu.findAll(ClassOrInterfaceDeclaration.class).stream()
            .filter(ClassOrInterfaceDeclaration::isInterface)
            .allMatch(iface -> iface.getMethods().size() < 5);
    }

    private boolean checkDependencyInversion(String code) {
        // Verificar se há dependências de abstrações (interfaces)
        Pattern interfacePattern = Pattern.compile("implements\\s+\\w+");
        Pattern abstractPattern = Pattern.compile("abstract\\s+class");
        return interfacePattern.matcher(code).find() || abstractPattern.matcher(code).find();
    }
}

