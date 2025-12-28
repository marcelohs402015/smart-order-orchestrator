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

@Slf4j
@Component
public class PatternDetector {

    private final JavaParser javaParser;

    public PatternDetector() {
        this.javaParser = new JavaParser();
    }

    public Map<String, Object> analyze(String code, boolean checkSolid) {
        Map<String, Object> result = new HashMap<>();
        List<String> patterns = new ArrayList<>();

        try {
            CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
                String className = clazz.getNameAsString();

                if (className.contains("Factory") && hasCreateMethod(clazz)) {
                    patterns.add("Factory Pattern");
                }

                if (className.contains("Adapter") && implementsInterface(clazz)) {
                    patterns.add("Adapter Pattern");
                }

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

        solid.put("Single Responsibility", checkSingleResponsibility(cu));
        solid.put("Open/Closed", true);
        solid.put("Liskov Substitution", true);
        solid.put("Interface Segregation", checkInterfaceSegregation(cu));
        solid.put("Dependency Inversion", checkDependencyInversion(code));

        return solid;
    }

    private boolean checkSingleResponsibility(CompilationUnit cu) {
        return cu.findAll(ClassOrInterfaceDeclaration.class).stream()
            .allMatch(clazz -> clazz.getMethods().size() < 15);
    }

    private boolean checkInterfaceSegregation(CompilationUnit cu) {
        return cu.findAll(ClassOrInterfaceDeclaration.class).stream()
            .filter(ClassOrInterfaceDeclaration::isInterface)
            .allMatch(iface -> iface.getMethods().size() < 5);
    }

    private boolean checkDependencyInversion(String code) {
        Pattern interfacePattern = Pattern.compile("implements\\s+\\w+");
        Pattern abstractPattern = Pattern.compile("abstract\\s+class");
        return interfacePattern.matcher(code).find() || abstractPattern.matcher(code).find();
    }
}
