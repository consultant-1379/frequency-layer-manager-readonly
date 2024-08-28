/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.service.events;

import java.util.Objects;

/**
 * Class for generic policy events.
 */
public class PolicyEvent {

    protected String nameSpace;
    protected String name;
    protected String version;
    protected String source;
    protected String target;

    protected PolicyEvent(final String nameSpace, final String name, final String version, final String source, final String target) {
        this.nameSpace = nameSpace;
        this.name = name;
        this.version = version;
        this.source = source;
        this.target = target;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PolicyEvent that = (PolicyEvent) o;
        return Objects.equals(nameSpace, that.nameSpace) &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(source, that.source) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameSpace, name, version, source, target);
    }

    @Override
    public String toString() {
        return String.format(
                "%s:: { nameSpace: '%s', name: '%s', version: '%s', source: '%s', target: '%s'}",
                getClass().getSimpleName(), nameSpace, name, version, source, target);
    }
}
