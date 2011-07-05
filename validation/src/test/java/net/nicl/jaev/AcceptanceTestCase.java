package net.nicl.jaev;

import org.junit.Test;

import static net.nicl.jaev.Acceptance.*;
import static net.nicl.jaev.ValidatorResultCode.ADDRESS_VALID;
import static net.nicl.jaev.ValidatorResultCode.GENERAL_VALIDATION_ERROR;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AcceptanceTestCase {

	@Test
	public void testSimpleValid() {

		assertThat(SIMPLE.accept(Result.create(ADDRESS_VALID, Validity.SYNTAX, null)), is(true));

		assertThat(SIMPLE.accept(Result.create(ADDRESS_VALID, Validity.DOMAIN, null)), is(true));

		assertThat(SIMPLE.accept(Result.create(ADDRESS_VALID, Validity.ACCOUNT, null)), is(true));

		assertThat(SIMPLE.accept(Result.create(ADDRESS_VALID, Validity.ACCESSIBLE, null)), is(true));

		assertThat(SIMPLE.accept(Result.create(GENERAL_VALIDATION_ERROR, Validity.DOMAIN, null)), is(true));

	}

	@Test
	public void testSimpleInvalid() {

	}

	@Test
	public void testAccountValid() {

		assertThat(ACCOUNT.accept(Result.create(ADDRESS_VALID, Validity.ACCOUNT, null)), is(true));

		assertThat(ACCOUNT.accept(Result.create(ADDRESS_VALID, Validity.ACCESSIBLE, null)), is(true));

		assertThat(ACCOUNT.accept(Result.create(GENERAL_VALIDATION_ERROR, Validity.DOMAIN, null)), is(true));
	}

	@Test
	public void testAccountInvalid() {
		assertThat(ACCOUNT.accept(Result.create(ADDRESS_VALID, Validity.SYNTAX, null)), is(false));

		assertThat(ACCOUNT.accept(Result.create(ADDRESS_VALID, Validity.DOMAIN, null)), is(false));
	}

	@Test
	public void testStrictValid() {

		assertThat(STRICT.accept(Result.create(ADDRESS_VALID, Validity.ACCESSIBLE, null)), is(true));

		assertThat(STRICT.accept(Result.create(GENERAL_VALIDATION_ERROR, Validity.DOMAIN, null)), is(true));
	}

	@Test
	public void testStrictInvalid() {
		assertThat(STRICT.accept(Result.create(ADDRESS_VALID, Validity.SYNTAX, null)), is(false));

		assertThat(STRICT.accept(Result.create(ADDRESS_VALID, Validity.DOMAIN, null)), is(false));

		assertThat(STRICT.accept(Result.create(ADDRESS_VALID, Validity.ACCOUNT, null)), is(false));
	}
}
