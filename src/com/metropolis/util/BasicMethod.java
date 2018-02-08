package com.metropolis.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"methodHandle"})
public class BasicMethod {

    private String name;
    private MethodHandle methodHandle;
    private List<Class<?>> validInputs = new ArrayList<>();

    public BasicMethod(final BasicMethod other) {
        this.name = other.getName();
        this.methodHandle = other.getMethodHandle();
        this.validInputs = other.getValidInputs();
    }
}
