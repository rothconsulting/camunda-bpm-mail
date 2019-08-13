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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.connect.impl.AbstractRequestInvocation;
import org.camunda.connect.spi.ConnectorRequestInterceptor;

import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;

public class CopyMailInvocation extends AbstractRequestInvocation<List<Message>> {

  protected final MailService mailService;

  public CopyMailInvocation(List<Message> messages, CopyMailRequest request, List<ConnectorRequestInterceptor> requestInterceptors, MailService mailService) {
    super(messages, request, requestInterceptors);
    this.mailService = mailService;
  }

  @Override
  public Object invokeTarget() throws Exception {

    CopyMailRequest request = (CopyMailRequest) getRequest();
	String srcFolderName  = request.getSrcFolder();
	String destFolderName = request.getDestFolder();

	Message[] newMessages = null;
	if ("copy".equalsIgnoreCase(request.getSupportUid()) ) {
		IMAPFolder srcFolder  = (IMAPFolder)mailService.ensureOpenFolder(srcFolderName);
		IMAPFolder destFolder = (IMAPFolder)mailService.ensureOpenFolder(destFolderName);

		AppendUID[] newMsgUID = srcFolder.copyUIDMessages(target.toArray(new Message[0]), destFolder);
		newMessages = getNewMessages(newMsgUID, destFolder);

		if ("move".equalsIgnoreCase(request.getMode())) {
			for (Message message : target) {
				message.setFlag(Flag.DELETED, true);
			}
		}
		
	} else if ("move".equalsIgnoreCase(request.getSupportUid()) ) {
		IMAPFolder srcFolder  = (IMAPFolder)mailService.ensureOpenFolder(srcFolderName);
		IMAPFolder destFolder = (IMAPFolder)mailService.ensureOpenFolder(destFolderName);

		AppendUID[] newMsgUID = srcFolder.moveUIDMessages(target.toArray(new Message[0]), destFolder);
		newMessages = getNewMessages(newMsgUID, destFolder);
		
	} else {
		Folder srcFolder  = mailService.ensureOpenFolder(srcFolderName);
		Folder destFolder = mailService.ensureOpenFolder(destFolderName);
		srcFolder.copyMessages(target.toArray(new Message[0]), destFolder);

		newMessages = target.toArray(new Message[0]);
		if ("move".equalsIgnoreCase(request.getMode())) {
			for (Message message : target) {
				message.setFlag(Flag.DELETED, true);
			}
			newMessages = new Message[0];
		}
	}
	
    return Arrays.asList(newMessages);
  }

  private Message[] getNewMessages(AppendUID[] newMsgUID, IMAPFolder destFolder) throws MessagingException {
		long[] uids = new long[newMsgUID.length];
		for (int i = 0; i < newMsgUID.length; i++) {
			uids[i] = newMsgUID[i].uid;
		}

		return destFolder.getMessagesByUID(uids);
  }
}
