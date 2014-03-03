package org.sisto.dpcm.wildfly;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sisto.dpcm.process.API.LifeCycleProcess.Phase;

public class WildflyClientTest {
	
	private WildflyClient client = null;
	
	@Before
	public void init() {
		client = new WildflyClient();
	}
	
	@After
	public void lize() {
		client = null;
	}
	
	@Test
	public void testDomainControllerBootHookNotImplemented() {
		boolean hooked = false;
		
		hooked = client.domainControllerBootHook(Phase.NONE);
		assertFalse("Domain hook is not used", hooked);
	}
	
	@Test
	public void testIsDomainControllerDefault() {
		boolean isDC = false;
		
		isDC = client.isDomainController();
		
		assertThat(isDC, is(false));
	}
	
	@Test
	public void testIsProcessRunningDefault() {
		boolean isPR = false;
		
		isPR = client.isProcessRunning();
		
		assertThat(isPR, is(false));
	}
	
	@Test
	public void testGracefulCommitSuicideManDefault() {
		boolean isCS = false;
		
		isCS = client.commitSuicideMan(true);
		
		assertThat(isCS, is(true));
	}
	
	@Test
	public void testUngracefulCommitSuicideManDefault() {
		boolean isCS = false;
		
		isCS = client.commitSuicideMan(false);
		
		assertThat(isCS, is(false));
	}
	
	@Test(expected = RuntimeException.class)  
	public void testNotExistingFunction() {
		boolean returns = false;
		
		returns = client.callFunction("thisfunctionnameshouldnotexist", Boolean.class, new Object[0]);
		
		assertThat(returns, is(false));
	}
}
