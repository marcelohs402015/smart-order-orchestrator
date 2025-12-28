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

@Slf4j
@Component
public class CodeAnalyzer {

    private final JavaParser javaParser;

    public CodeAnalyzer() {
        this.javaParser = new JavaParser();
    }

    public Map<String, Object> analyze(String code, String focus) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> violations = new ArrayList<>();

        try {
            CompilationUnit cu = javaParser.parse(code).getResult().orElseThrow();

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
                int methodCount = clazz.getMethods().size();
                if (methodCount > 20) {
                    violations.add(Map.of(
                        "type", "class_size",
                        "message", String.format("Class %s has %d methods. Consider splitting.", clazz.getNameAsString(), methodCount),
                        "severity", "warning"
                    ));
                }

                clazz.getMethods().forEach(method -> {
                    int lineCount = method.getEnd().map(end -> end.line - method.getBegin().map(begin -> begin.line).orElse(0)).orElse(0);
                    if (lineCount > 50) {
                        violations.add(Map.of(
                            "type", "method_length",
                            "message", String.format("Method %s is too long (%d lines). Consider refactoring.", method.getNameAsString(), lineCount),
                            "severity", "warning"
                        ));
                    }
                });
            });

            result.put("violations", violations);
            result.put("summary", String.format("Found %d potential issues", violations.size()));
            result.put("focus", focus);

        } catch (Exception e) {
            log.error("Error analyzing code", e);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
