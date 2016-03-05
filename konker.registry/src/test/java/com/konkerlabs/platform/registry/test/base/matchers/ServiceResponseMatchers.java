package com.konkerlabs.platform.registry.test.base.matchers;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

public class ServiceResponseMatchers {

    private static abstract class BaseMatcher<ServiceResponse> extends TypeSafeMatcher<ServiceResponse> {

        protected void describeMismatchSafely(ServiceResponse item, Description mismatchDescription) {
            mismatchDescription.appendText("was ").appendValue(item);
        }
    }

    public static Matcher<ServiceResponse> isResponseOk() {
        return new BaseMatcher<ServiceResponse>() {
            @Override
            protected boolean matchesSafely(ServiceResponse item) {
                return item.getStatus().equals(ServiceResponse.Status.OK) &&
                       item.getResponseMessages().isEmpty();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(
                    ServiceResponse.builder().status(ServiceResponse.Status.OK).build().toString()
                );
            }
        };
    }

    public static Matcher<ServiceResponse> hasErrorMessage(String message) {
        return new BaseMatcher<ServiceResponse>() {
            @Override
            protected boolean matchesSafely(ServiceResponse item) {
                return item.getStatus().equals(ServiceResponse.Status.ERROR) &&
                       item.getResponseMessages().contains(message);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(
                    ServiceResponse.builder()
                        .status(ServiceResponse.Status.ERROR)
                        .responseMessage(message)
                        .build().toString()
                );
            }
        };
    }

    public static Matcher<ServiceResponse> hasAllErrors(List<String> errors) {
        return new BaseMatcher<ServiceResponse>() {
            @Override
            protected boolean matchesSafely(ServiceResponse item) {
                return item.getStatus().equals(ServiceResponse.Status.ERROR) &&
                       item.getResponseMessages().equals(errors);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(
                        ServiceResponse.builder()
                                .status(ServiceResponse.Status.ERROR)
                                .responseMessages(errors)
                                .build().toString()
                );
            }
        };
    }
}
