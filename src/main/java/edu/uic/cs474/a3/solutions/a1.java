package edu.uic.cs474.a3.solutions;


import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import edu.uic.cs474.a3.OverloadOverrideFinder;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class a1 implements OverloadOverrideFinder {


    @Override
    public Map<String, Map<String, Integer>> getOverloads(Map<String, ClassOrInterfaceDeclaration> classes) {
        Map<String, Map<String, Integer>> result = new HashMap<>();

        for(Map.Entry<String, ClassOrInterfaceDeclaration> e : classes.entrySet()) {
            Map<String, Integer> map = new HashMap<>();
            result.put(e.getKey(), map);

            for(MethodDeclaration m : e.getValue().getMethods()) {
                map.put(m.getNameAsString(), map.getOrDefault(m.getNameAsString(),0) + 1);
            }
            {
                Set<String> notOverloadedMethods = new HashSet<>();
                map.keySet().forEach(k -> {
                    if(map.get(k) == 1)
                        notOverloadedMethods.add(k);
                });
                notOverloadedMethods.forEach(map::remove);

            }
        }
        return result;
    }

    private Set<MethodDeclaration> findInheritedMethod(ClassOrInterfaceDeclaration c, Map<String, ClassOrInterfaceDeclaration> classes) {
        AtomicReference<Stream<MethodDeclaration>> stream = new AtomicReference<>();
        AtomicReference<Stream<MethodDeclaration>> stream1 = new AtomicReference<>(Stream.empty());
        while (c.getExtendedTypes().getFirst().isPresent()) {
            c = classes.get(c.getExtendedTypes().getFirst().get().getNameAsString());
            MethodDeclaration[] temp = c.getMethods().toArray(new MethodDeclaration[0]);
            Stream<MethodDeclaration> stream2 = Arrays.stream(temp);
            stream2.forEach(m -> {
                stream.set(Stream.of(m));
                stream1.set(Stream.concat(stream.get(), stream1.get()));
            });

        }
        return stream1.get().collect(Collectors.toSet());
    }

    private List<String> turnMethodIntoList(MethodDeclaration m) {
        List<String> methodName = new LinkedList<>();
        methodName.add(m.getNameAsString());
        List<String> parameters = new LinkedList<>();
        NodeList<Parameter> temp = m.getParameters();
        temp.forEach(t1 -> {
            parameters.add(t1.getNameAsString());
        });
        Stream<String> stream1 = methodName.stream();
        Stream<String> stream2 = parameters.stream();

        return Stream.concat(stream1, stream2).collect(Collectors.toList());
    }

    @Override
    public Map<List<String>, Set<String>> getOverrides(Map<String, ClassOrInterfaceDeclaration> classes) {
        Map<List<String>, Set<String>> result = new HashMap<>();

        for(Map.Entry<String, ClassOrInterfaceDeclaration> e : classes.entrySet()) {
            Set<MethodDeclaration> inheritedMethods = findInheritedMethod(e.getValue(),classes);
            List<MethodDeclaration> declardMethods = e.getValue().getMethods();

            for(MethodDeclaration declared : declardMethods) {
                if(inheritedMethods.contains(declared)) {
                    List<String> methodAsList = turnMethodIntoList(declared);

                    Set<String> classesThatOverrideThisMethod = result.getOrDefault(methodAsList, new HashSet<>());
                    classesThatOverrideThisMethod.add(e.getKey());
                    result.put(methodAsList, classesThatOverrideThisMethod);
                }
            }
        }

        return result;
    }

}
