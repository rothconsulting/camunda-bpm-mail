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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.OrTerm;

import org.camunda.bpm.extension.mail.EmptyResponse;
import org.camunda.bpm.extension.mail.MailConnectorException;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.bpm.extension.mail.service.MailServiceFactory;
import org.camunda.connect.impl.AbstractConnector;
import org.camunda.connect.spi.ConnectorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyMailConnector extends AbstractConnector<CopyMailRequest, EmptyResponse> {

  private final static Logger LOGGER = LoggerFactory.getLogger(CopyMailConnector.class);

  public static final String CONNECTOR_ID = "mail-copy";

  protected MailConfiguration configuration;

  public CopyMailConnector() {
    super(CONNECTOR_ID);
  }

  @Override
  public CopyMailRequest createRequest() {
    return new CopyMailRequest(this, getConfiguration());
  }

  @Override
  public ConnectorResponse execute(CopyMailRequest request) {
    MailService mailService = MailServiceFactory.getService(getConfiguration());

    try {

      Folder srcFolder = mailService.ensureOpenFolder(request.getSrcFolder());
      List<Message> messages = Arrays.asList(getMessages(srcFolder, request));

      CopyMailInvocation invocation = new CopyMailInvocation(messages, request, requestInterceptors, mailService);

      invocation.proceed();

      return new EmptyResponse();

    } catch (Exception e) {
      throw new MailConnectorException("failed to copy mails", e);
    }
  }

  protected Message[] getMessages(Folder folder, CopyMailRequest request) throws MessagingException {

    if (request.getMails() != null) {
      LOGGER.debug("copy mails: {}", request.getMails());

      List<String> messageIds = collectMessageIds(request.getMails());
      return getMessagesByIds(folder, messageIds);

    } else if (request.getMessageIds() != null) {
      LOGGER.debug("copy mails with message ids: {}", request.getMessageIds());

      return getMessagesByIds(folder, request.getMessageIds());

    } else {
      LOGGER.debug("copy mails with message numbers: {}", request.getMessageNumbers());

      int[] numbers = request.getMessageNumbers().stream().mapToInt(i -> i).toArray();
      return folder.getMessages(numbers);
    }
  }

  protected List<String> collectMessageIds(List<Mail> mails) {

    return mails.stream()
        .map(m -> Optional.ofNullable(m.getMessageId()).orElse(""))
        .filter(id -> !id.isEmpty())
        .collect(Collectors.toList());
  }

  protected Message[] getMessagesByIds(Folder folder, List<String> messageIds) throws MessagingException {

    List<MessageIDTerm> idTerms = messageIds.stream()
        .map(MessageIDTerm::new)
        .collect(Collectors.toList());

    OrTerm searchTerm = new OrTerm(idTerms.toArray(new MessageIDTerm[idTerms.size()]));

    return folder.search(searchTerm);
  }

  protected MailConfiguration getConfiguration() {
    if (configuration == null) {
      configuration = MailConfigurationFactory.getConfiguration();
    }
    return configuration;
  }

  public void setConfiguration(MailConfiguration configuration) {
    this.configuration = configuration;
  }

}
