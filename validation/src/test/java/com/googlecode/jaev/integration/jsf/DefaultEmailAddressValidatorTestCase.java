package com.googlecode.jaev.integration.jsf;

import static com.googlecode.jaev.integration.Initialiser.defaultFromAddress;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jaev.mail.MailParseException;

@RunWith(JMock.class)
public final class DefaultEmailAddressValidatorTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEmailAddressValidatorTestCase.class);

    private static DefaultEmailAddressValidator addressValidator;

    private final Mockery context = new JUnit4Mockery() {

        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    static {
        try {
            addressValidator = new DefaultEmailAddressValidator(defaultFromAddress());
        } catch (MailParseException e) {
            LOG.error("Unable to create address validator.", e);
        }

    }

    @Test
    @Ignore
    public void testUnknown() {
        test("jaev@gmail.com", "The e-mail address is unknown");
    }

    @Test
    public void testDomainNotFound() {
        test("jaev@googlecote.com", "The domain 'googlecote.com' is not valid.");
    }

    @Test
    @Ignore("No non-email acception domain could be found at the moment.")
    public void testNoMailServerFound() {
        test("jaev@gmeil.com", "The domain 'gmeil.com' does not accept e-mails.");
    }

    @Test
    public void testInvalidCharacters() {
        test("jae\u008fv@googlecode.com", "The e-mail address contains invalid characters.");
    }

    @Test
    public void testInvalidFormat() {
        test("jaev.googlecode.com", "The format of the e-mail address is invalid.");
    }

    @Test
    public void testInvalidUsername() {
        test("jaev.@googlecode.com", "The user name 'jaev.' is not valid.");
    }

    @Test
    public void testInvalidDomain() {
        test("jaev@googlecode.c", "The domain 'googlecode.c' is not valid.");
    }

    @Test
    public void testInvalidToplevelDomain() {
        test("jaev@googlecode.yy", "The top level domain 'yy' is not valid.");
    }

    private void test(String mailAddress, String expectedMessage) {
        final FacesContext facesContext = context.mock(FacesContext.class);
        final UIViewRoot viewRoot = context.mock(UIViewRoot.class);
        final UIComponent component = context.mock(UIComponent.class);

        context.checking(new Expectations() {

            {
                oneOf(viewRoot).getLocale();
                will(returnValue(Locale.ENGLISH));
                oneOf(facesContext).getViewRoot();
                will(returnValue(viewRoot));
            }
        });

        try {
            addressValidator.validate(facesContext, component, mailAddress);

            fail("Validation of '" + mailAddress + "' did not fail.");

        } catch (ValidatorException ve) {
            assertThat(ve.getFacesMessage().getDetail(), is(expectedMessage));
        }
    }

}
