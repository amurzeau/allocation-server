package org.amurzeau.allocation.rest;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
@RegisterForReflection
public class ProjectUpdate {
    public String name;

    public String board;

    public String component;

    public String arch;

    public String type;

    public List<String> eotpOpen;

    public List<String> eotpClosed;
}
