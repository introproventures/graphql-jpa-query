package com.introproventures.graphql.jpa.query.introspection;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanUtilTest {

    @Test
    public void testGetBeanGetterNameNull() {
        assertThat(BeanUtil.getBeanGetterName(null)).isNull();
    }
    
    @Test
    public void testGetBeanSetterNameNull() {
        assertThat(BeanUtil.getBeanSetterName(null)).isNull();
    }
    
    @Test
    public void testIsBeanSetterNameNull() {
        assertThat(BeanUtil.isBeanGetter(null)).isFalse();
    }
    
    @Test
    public void testIsBeanGetterGetMethod() throws NoSuchMethodException, SecurityException {
        // given
        Method subject = TestBean.class.getDeclaredMethod("getFoo", new Class[] {}); 

        // then
        assertThat(BeanUtil.isBeanGetter(subject)).isTrue();
    }

    @Test
    public void testIsBeanGetterIsMethod() throws NoSuchMethodException, SecurityException {
        // given
        Method subject = TestBean.class.getDeclaredMethod("isBar", new Class[] {}); 

        // then
        assertThat(BeanUtil.isBeanGetter(subject)).isTrue();
    }

    @Test
    public void testIsBeanGetterToString() throws NoSuchMethodException, SecurityException {
        // given
        Method subject = TestBean.class.getDeclaredMethod("toString", new Class[] {}); 

        // then
        assertThat(BeanUtil.isBeanGetter(subject)).isFalse();
    }
    
    @Test
    public void testGetBeanGetterNameObjectMethod() throws NoSuchMethodException, SecurityException {
        // given
        Method subject = Object.class.getDeclaredMethod("hashCode", new Class[] {}); 
        
        // then
        assertThat(BeanUtil.getBeanGetterName(subject)).isNull();
    }

    @Test
    public void testIsBeanPropertyObjectMethod() throws NoSuchMethodException, SecurityException {
        // given
        Method subject = Object.class.getDeclaredMethod("hashCode", new Class[] {}); 
        
        // then
        assertThat(BeanUtil.isBeanProperty(subject)).isFalse();
    }
    
    @Test
    public void testIsBeanPropertyGetMethod() throws NoSuchMethodException, SecurityException {
        // given
        Method subject = TestBean.class.getDeclaredMethod("getFoo", new Class[] {}); 
        
        // then
        assertThat(BeanUtil.isBeanProperty(subject)).isTrue();
    }

    @Test
    public void testIsBeanPropertyIsMethod() throws NoSuchMethodException, SecurityException {
        // given
        Method subject = TestBean.class.getDeclaredMethod("isBar", new Class[] {}); 
        
        // then
        assertThat(BeanUtil.isBeanProperty(subject)).isTrue();
    }

    @Test
    public void testIsBeanPropertySetMethod() throws NoSuchMethodException, SecurityException {
        // given
        Method subject = TestBean.class.getDeclaredMethod("setBar", new Class[] { boolean.class }); 
        
        // then
        assertThat(BeanUtil.isBeanProperty(subject)).isTrue();
    }
    
    
    static class TestBean {
        private String foo;
        private boolean bar;
        
        public String getFoo() {
            return foo;
        }
        
        public void setFoo(String foo) {
            this.foo = foo;
        }
        
        public boolean isBar() {
            return bar;
        }
        
        public void setBar(boolean bar) {
            this.bar = bar;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("TestBean [foo=").append(foo).append(", bar=").append(bar).append("]");
            return builder.toString();
        }
    }

}
