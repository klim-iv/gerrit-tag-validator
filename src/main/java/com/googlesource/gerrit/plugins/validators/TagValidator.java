// Copyright (C) 2012 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.googlesource.gerrit.plugins.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.extensions.annotations.Listen;
import com.google.gerrit.server.events.CommitReceivedEvent;
import com.google.gerrit.server.git.validators.CommitValidationException;
import com.google.gerrit.server.git.validators.CommitValidationListener;
import com.google.gerrit.server.git.validators.CommitValidationMessage;
import com.google.inject.Singleton;
import com.google.inject.Inject;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.git.GitRepositoryManager;

@Listen
@Singleton
public class TagValidator implements CommitValidationListener {
  private static Logger log = LoggerFactory.getLogger(TagValidator.class);

  private String pattern;
  private String errorMessage = "";

  private final String pluginName;
  private final PluginConfigFactory cfgFactory;
  private final GitRepositoryManager repoManager;

  @Inject
  TagValidator(@PluginName String pluginName,
      PluginConfigFactory cfgFactory, GitRepositoryManager repoManager) {
    this.pluginName = pluginName;
    this.cfgFactory = cfgFactory;
    this.repoManager = repoManager;
  }

  @Override
  public List<CommitValidationMessage> onCommitReceived(CommitReceivedEvent receiveEvent)
      throws CommitValidationException {
    final String commitMessage = receiveEvent.commit.getFullMessage();
    final List<CommitValidationMessage> messages = new ArrayList<CommitValidationMessage>();

    if (receiveEvent.refName.equals("refs/meta/config"))
      return messages;

    try {
    PluginConfig cfg =
          cfgFactory.getFromProjectConfig(
              receiveEvent.project.getNameKey(), pluginName);

      pattern = cfg.getString("regexp", "");
      errorMessage = cfg.getString("errorMessage", "");
    } catch(Exception e) {
      pattern = "";
      log.warn("TagValidator> Exception on read config for plugin: "
          + e.getMessage());
    }

    Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
    if (pattern.length() > 0 && !p.matcher(commitMessage).matches()) {
      log.warn("TagValidator> Commit "
          + receiveEvent.commit.getId().getName() + " REJECTED (" + errorMessage + ")");
      throw new CommitValidationException("Wrong commit message: " + errorMessage);
    }
    return messages;
  }
}
