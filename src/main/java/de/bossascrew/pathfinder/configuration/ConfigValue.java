package de.bossascrew.pathfinder.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigValue {

	String path();
	String[] comments() default "";
}