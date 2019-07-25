/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.extension.mail.copy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.mail.MailTestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

public class CopyMailConnectorProcessTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  @Rule
  public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

  private Folder destFolder = null;
  
  @Before
  public void createMails() throws Exception {
    greenMail.setUser("test@camunda.com", "bpmn");

    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-1", "body1");
    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-2", "body2");
    
    if (destFolder == null) {
    	destFolder = MailTestUtil.createFolder("DONE", greenMail);
    }
    MailTestUtil.clearFolder(destFolder);

  }

  @Test
  @Deployment(resources = "processes/mail-copy.bpmn")
  public void copyMailById() throws MessagingException, IOException {

    MimeMessage[] mails = greenMail.getReceivedMessages();
    String messageId = mails[0].getMessageID();

    engineRule.getRuntimeService().startProcessInstanceByKey("copy-mail",
        Variables.createVariables().putValue("messageId", messageId).putValue("destFolder", "DONE").putValue("mode", "copy"));

    try {
		Thread.sleep(1000L);
	} catch (InterruptedException e) {}  

    Message[] copiedMails = destFolder.getMessages();

    String subject = copiedMails[0].getSubject();
    assertEquals("mail-1", subject);
    
    assertThat(copiedMails).hasSize(1);
    assertThat(copiedMails[0].isSet(Flag.DELETED)).isFalse();

    assertThat(mails[0].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  @Deployment(resources = "processes/mail-copy.bpmn")
  public void moveMailById() throws Exception {

    MimeMessage[] mails = greenMail.getReceivedMessages();
    String messageId = mails[0].getMessageID();

	Folder destFolder = MailTestUtil.createFolder("DONE", greenMail);
	
    engineRule.getRuntimeService().startProcessInstanceByKey("copy-mail",
        Variables.createVariables().putValue("messageId", messageId).putValue("destFolder", "DONE").putValue("mode", "move"));

    try {
		Thread.sleep(1000L);
	} catch (InterruptedException e) {}  

    Message[] copiedMails = destFolder.getMessages();

    String subject = copiedMails[0].getSubject();
    assertEquals("mail-1", subject);
    
    assertThat(copiedMails).hasSize(1);
    assertThat(copiedMails[0].isSet(Flag.DELETED)).isFalse();
    
    assertThat(mails[0].isSet(Flag.DELETED)).isTrue();
  }

}
