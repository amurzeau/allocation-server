package org.amurzeau.allocation.rest;

import javax.persistence.Entity;

import io.quarkus.runtime.annotations.RegisterForReflection;

@Entity
@RegisterForReflection
public class ApplicationType extends NamedItem {
}
