package com.introproventures.graphql.jpa.query.schema.fixtures;


public class VariableValue<T> {

    private T value;

    public VariableValue() {
    }

    public VariableValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
    
    
    /**
     * Encountered Java type [class org.activiti.cloud.services.query.model.VariableValue] for which we could not locate a JavaTypeDescriptor 
     * and which does not appear to implement equals and/or hashCode.  This can lead to significant performance problems when performing 
     * equality/dirty checking involving this Java type.  
     * 
     * Consider registering a custom JavaTypeDescriptor or at least implementing equals/hashCode. 
     * 
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VariableValue<?> other = (VariableValue<?>) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "VariableValue [value=" + value + "]";
    }
    
}