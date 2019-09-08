package com.introproventures.graphql.jpa.query.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.introproventures.graphql.jpa.query.schema.impl.Order;

import lombok.Data;

public class OrderByTest {

    @Test
    public void testOrderByFieldsArray() {

        List<Employee> result = Stream.of(getUnsortedEmployees())
                                      .sorted(Order.by("firstName", "lastName"))
                                      .collect(Collectors.toList());
        
        assertThat(result).containsExactly(new Employee(7l, "Alex", "Beckham"),
                                           new Employee(1l, "Alex", "Gussin"),
                                           new Employee(4l, "Brian", "Sux"),
                                           new Employee(6l, "Brian", "Suxena"),
                                           new Employee(3l, "David", "Beckham"),
                                           new Employee(2l, "Lokesh", "Gupta"),
                                           new Employee(5l, "Neon", "Piper"));
    }
    
    @Test
    public void testOrderByFieldsReversed() {

        List<Employee> result = Stream.of(getUnsortedEmployees())
                                      .sorted(Order.by("firstName", "lastName").reversed())
                                      .collect(Collectors.toList());
        
        assertThat(result).containsExactly(
                                           new Employee(5l, "Neon", "Piper"),
                                           new Employee(2l, "Lokesh", "Gupta"),
                                           new Employee(3l, "David", "Beckham"),
                                           new Employee(6l, "Brian", "Suxena"),
                                           new Employee(4l, "Brian", "Sux"),
                                           new Employee(1l, "Alex", "Gussin"),
                                           new Employee(7l, "Alex", "Beckham")
                                           );
    }

    @Test
    public void testOrderByFieldChained() {

        List<Employee> result = Stream.of(getUnsortedEmployees())
                                      .sorted(Order.by("firstName")
                                                     .thenComparing(Order.by("lastName")))
                                      .collect(Collectors.toList());
        
        assertThat(result).containsExactly(new Employee(7l, "Alex", "Beckham"),
                                           new Employee(1l, "Alex", "Gussin"),
                                           new Employee(4l, "Brian", "Sux"),
                                           new Employee(6l, "Brian", "Suxena"),
                                           new Employee(3l, "David", "Beckham"),
                                           new Employee(2l, "Lokesh", "Gupta"),
                                           new Employee(5l, "Neon", "Piper"));
    }

    @Test
    public void testOrderByFieldChainedReversed() {

        List<Employee> result = Stream.of(getUnsortedEmployees())
                                      .sorted(Order.by("firstName").reversed()
                                                     .thenComparing(Order.by("lastName").reversed()))
                                      .collect(Collectors.toList());
        
        assertThat(result).containsExactly(new Employee(5l, "Neon", "Piper"),
                                           new Employee(2l, "Lokesh", "Gupta"),
                                           new Employee(3l, "David", "Beckham"),
                                           new Employee(6l, "Brian", "Suxena"),
                                           new Employee(4l, "Brian", "Sux"),
                                           new Employee(1l, "Alex", "Gussin"),
                                           new Employee(7l, "Alex", "Beckham"));
    }

    @Test
    public void testOrderByIdAscending() {

        List<Employee> result = Stream.of(getUnsortedEmployees())
                                      .sorted(Order.by("id"))
                                      .collect(Collectors.toList());
        
        assertThat(result).containsExactly(new Employee(1l, "Alex", "Gussin"),
                                           new Employee(2l, "Lokesh", "Gupta"),
                                           new Employee(3l, "David", "Beckham"),
                                           new Employee(4l, "Brian", "Sux"),
                                           new Employee(5l, "Neon", "Piper"),
                                           new Employee(6l, "Brian", "Suxena"),
                                           new Employee(7l, "Alex", "Beckham"));
    }

    @Test
    public void testOrderByIdDescending() {

        List<Employee> result = Stream.of(getUnsortedEmployees())
                                      .sorted(Order.by("id").reversed())
                                      .collect(Collectors.toList());
        
        assertThat(result).containsExactly(new Employee(7l, "Alex", "Beckham"),
                                           new Employee(6l, "Brian", "Suxena"),
                                           new Employee(5l, "Neon", "Piper"),
                                           new Employee(4l, "Brian", "Sux"),
                                           new Employee(3l, "David", "Beckham"),
                                           new Employee(2l, "Lokesh", "Gupta"),
                                           new Employee(1l, "Alex", "Gussin"));
    }
    

    @Test
    public void testOrderByComparators() {

        List<Employee> result = Stream.of(getUnsortedEmployees())
                                      .sorted(Comparator.comparing(Employee::getFirstName)
                                                        .thenComparing(Employee::getLastName))
                                      .collect(Collectors.toList());
        
        assertThat(result).containsExactly(new Employee(7l, "Alex", "Beckham"),
                                           new Employee(1l, "Alex", "Gussin"),
                                           new Employee(4l, "Brian", "Sux"),
                                           new Employee(6l, "Brian", "Suxena"),
                                           new Employee(3l, "David", "Beckham"),
                                           new Employee(2l, "Lokesh", "Gupta"),
                                           new Employee(5l, "Neon", "Piper"));
        
    }
    
    private static Employee[] getUnsortedEmployees()
    {
        ArrayList<Employee> list = new ArrayList<>();
        
        list.add( new Employee(2l, "Lokesh", "Gupta") );
        list.add( new Employee(1l, "Alex", "Gussin") );
        list.add( new Employee(4l, "Brian", "Sux") );
        list.add( new Employee(5l, "Neon", "Piper") );
        list.add( new Employee(3l, "David", "Beckham") );
        list.add( new Employee(7l, "Alex", "Beckham") );
        list.add( new Employee(6l, "Brian", "Suxena") );
        
        return list.toArray(new Employee[] {});
    }    
    
    @Data
    static class Employee {
        private final long id;
        private final String firstName;
        private final String lastName;
    }
    
}
