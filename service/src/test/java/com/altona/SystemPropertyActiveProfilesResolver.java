package com.altona;

import com.google.common.collect.Sets;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;

import java.util.Set;

import static com.altona.Main.getOsProfile;

public class SystemPropertyActiveProfilesResolver implements ActiveProfilesResolver {

    private final DefaultActiveProfilesResolver defaultActiveProfilesResolver = new DefaultActiveProfilesResolver();

    @Override
    public String[] resolve(Class<?> testClass) {
        final String springProfileKey = "spring.profiles.active";
        Set<String> profiles = Sets.newHashSet();

        String[] system = System.getProperties().containsKey(springProfileKey)
                ? System.getProperty(springProfileKey).split("\\s*,\\s*")
                : new String[0];

        String[] annotation = defaultActiveProfilesResolver.resolve(testClass);

        profiles.addAll(Sets.newHashSet(system));
        profiles.addAll(Sets.newHashSet(annotation));

        profiles.add(getOsProfile());

        return profiles.toArray(new String[]{});
    }


}