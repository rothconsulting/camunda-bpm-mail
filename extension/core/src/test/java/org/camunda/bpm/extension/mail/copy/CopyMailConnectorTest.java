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
import static org.mockito.Mockito.mock;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.camunda.bpm.extension.mail.MailConnectors;
import org.camunda.bpm.extension.mail.MailTestUtil;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.copy.CopyMailConnector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

public class CopyMailConnectorTest {

  private static final String SRC_FOLDER  = "INBOX";
  private static final String DEST_FOLDER = "DONE";
	
  private Folder destFolder = null;

  @Rule
  public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void createMails() throws Exception {
    greenMail.setUser("test@camunda.com", "bpmn");

    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-1", "body");
    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-2", "body");

   	destFolder = MailTestUtil.createFolder(DEST_FOLDER, greenMail);
    MailTestUtil.clearFolder(destFolder);
  }

  @Test
  public void copyMailByNumber() throws MessagingException {
	  
    MimeMessage[] mails = greenMail.getReceivedMessages();

    MailConnectors.copyMails()
      .createRequest()
      .srcFolder(SRC_FOLDER)
      .destFolder(DEST_FOLDER)
      .messageNumbers(1)
      .execute();
    
    try {
		Thread.sleep(1000L);
	} catch (InterruptedException e) {}  

    Message[] copiedMails = destFolder.getMessages();
    assertThat(copiedMails).hasSize(1);
    assertThat(copiedMails[0].isSet(Flag.DELETED)).isFalse();

    assertThat(mails[0].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  public void copyMailById() throws MessagingException {

    MimeMessage[] mails = greenMail.getReceivedMessages();
    String messageId = mails[0].getMessageID();

    MailConnectors.copyMails()
      .createRequest()
      .srcFolder(SRC_FOLDER)
      .destFolder(DEST_FOLDER)
      .messageIds(messageId)
      .execute();
    
    try {
		Thread.sleep(1000L);
	} catch (InterruptedException e) {}  

    Message[] copiedMails = destFolder.getMessages();
    assertThat(copiedMails).hasSize(1);
    assertThat(copiedMails[0].isSet(Flag.DELETED)).isFalse();

    assertThat(mails[0].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  public void moveMailByNumber() throws MessagingException {
	  
    MimeMessage[] mails = greenMail.getReceivedMessages();

    MailConnectors.copyMails()
      .createRequest()
      .srcFolder(SRC_FOLDER)
      .destFolder(DEST_FOLDER)
      .mode("move")
      .messageNumbers(1)
      .execute();
    
    try {
		Thread.sleep(1000L);
	} catch (InterruptedException e) {}  

    Message[] copiedMails = destFolder.getMessages();
    assertThat(copiedMails).hasSize(1);
    assertThat(copiedMails[0].isSet(Flag.DELETED)).isFalse();

    assertThat(mails[0].isSet(Flag.DELETED)).isTrue();
  }

  @Test
  public void moveMailById() throws MessagingException {

    MimeMessage[] mails = greenMail.getReceivedMessages();
    String messageId = mails[0].getMessageID();

    MailConnectors.copyMails()
      .createRequest()
      .srcFolder(SRC_FOLDER)
      .destFolder(DEST_FOLDER)
      .mode("move")
      .messageIds(messageId)
      .execute();
    
    try {
		Thread.sleep(1000L);
	} catch (InterruptedException e) {}  

    Message[] copiedMails = destFolder.getMessages();
    assertThat(copiedMails).hasSize(1);
    assertThat(copiedMails[0].isSet(Flag.DELETED)).isFalse();

    assertThat(mails[0].isSet(Flag.DELETED)).isTrue();
  }

/*
  @Test
  public void copyMailByGivenMail() throws MessagingException {

    Mail mail = MailConnectors.pollMails()
      .createRequest()
        .folder(SRC_FOLDER)
        .downloadAttachments(false)
      .execute()
      .getMails()
      .get(0);

    MailConnectors.copyMails()
      .createRequest()
        .srcFolder(SRC_FOLDER)
        .destFolder(SRC_FOLDER)
        .mails(mail)
      .execute();

    try {
		Thread.sleep(1000L);
	} catch (InterruptedException e) {}  

    Message[] copiedMails = destFolder.getMessages();
    assertThat(copiedMails).hasSize(1);
    assertThat(copiedMails[0].isSet(Flag.DELETED)).isFalse();
    
    try {
		Thread.sleep(1000L);
	} catch (InterruptedException e) {}  
  }
*/
  
  @Test
  public void folderFromConfiguration() throws MessagingException {

    MailConnectors.copyMails()
      .createRequest()
        .messageNumbers(1)
      .execute();

    try {
		Thread.sleep(1000L);
	} catch (InterruptedException e) {}  

    Message[] copiedMails = destFolder.getMessages();
    assertThat(copiedMails).hasSize(1);
    assertThat(copiedMails[0].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  public void missingFolder() throws MessagingException {
    CopyMailConnector connector = new CopyMailConnector();
    connector.setConfiguration(mock(MailConfiguration.class));

    thrown.expect(RuntimeException.class);
    thrown.expectMessage("The request is invalid");

    connector
      .createRequest()
        .messageNumbers(0)
      .execute();
  }

  @Test
  public void missingMessageCriteria() throws MessagingException {

    thrown.expect(RuntimeException.class);
    thrown.expectMessage("The request is invalid");

    MailConnectors.copyMails()
      .createRequest()
      .srcFolder(SRC_FOLDER)
      .destFolder(DEST_FOLDER)
      .execute();
  }

}
