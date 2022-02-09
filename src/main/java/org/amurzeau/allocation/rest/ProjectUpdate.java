package org.amurzeau.allocation.rest;

import java.util.List;

import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public class ProjectUpdate {
    public String name;
    
    public String board;
    
    public String component;
    
    public String arch;

    public String type;

    public List<String> eotpOpen;

    public List<String> eotpClosed;
}
