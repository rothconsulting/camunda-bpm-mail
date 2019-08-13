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
package org.camunda.bpm.extension.mail.config;

import java.time.Duration;
import java.util.Properties;

/**
 * Configuration of the mail extension.
 *
 */
public interface MailConfiguration {

  /**
   * @return user name for connecting to the mail provider
   */
  String getUserName();

  /**
   * @return password for connecting to the mail provider
   */
  String getPassword();

  /**
   * @return configuration of the mail protocols (e.g. smtp, imaps)
   *
   * @see <a href="https://javamail.java.net/nonav/docs/api/com/sun/mail/imap/package-summary.html">Package com.sun.mail.imap</a>
   * @see <a href="https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html">Package com.sun.mail.smtp</a>
   */
  Properties getProperties();

  /**
   * @return default folder for polling mails from (e.g. INBOX), can be <code>null</code>
   */
  String getPollFolder();

  /**
   * @return default sender (i.e. from) for sending mails, can be <code>null</code>
   */
  String getSender();

  /**
   * @return default sender alias (i.e. from alias) for sending mails, can be <code>null</code>
   */
  String getSenderAlias();

  /**
   * @return <code>true</code> if attachments of mails should always be stored locally
   */
  boolean downloadAttachments();

  /**
   * @return path to directory where the attachments should be stored in
   */
  String getAttachmentPath();

  /**
   * @return default duration between two polling requests from notification service
   */
  Duration getNotificationLookupTime();

  /**
   * @return the copy mode of "copy" or "move"
   */
  String getCopyMode();

  /**
   * @return the copy source folder
   */
  String getCopySrcFolder();

  /**
   * @return the copy destination folder
   */
  String getCopyDestFolder();

  /**
   * 
   * @return true or false whether the mail server supports UID
   */
  String getSuppportUid();

}