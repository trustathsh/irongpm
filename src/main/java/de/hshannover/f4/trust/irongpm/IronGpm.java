/*
 * #%L
 * =====================================================
 *   _____                _     ____  _   _       _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                             \____/
 *
 * =====================================================
 *
 * Hochschule Hannover
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 *
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de/
 *
 * This file is part of irongpm, version 0.1.0,
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2014 - 2015 Trust@HsH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 * #L%
 */
package de.hshannover.f4.trust.irongpm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.hshannover.f4.trust.ironcommon.properties.Properties;
import de.hshannover.f4.trust.irongpm.algorithm.BasicMatchingAlgorithm;
import de.hshannover.f4.trust.irongpm.algorithm.RuleWrapper;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternRule;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.RuleLoader;
import de.hshannover.f4.trust.irongpm.algorithm.util.IfmapPublishUtil;
import de.hshannover.f4.trust.irongpm.ifmap.PolicyPublisher;
import de.hshannover.f4.trust.irongpm.ifmap.SelfPublisher;
import de.hshannover.f4.trust.irongpm.listener.LoggingListener;
import de.hshannover.f4.trust.irongpm.util.FileUtils;
import de.hshannover.f4.trust.irongpm.util.ReflectionUtils;

/**
 * This class starts the application.
 *
 * @author Leonard Renners
 * @author Bastian Hellmann
 *
 */
public final class IronGpm extends ClassLoader {

	private static Pulldozer pulldozer;
	private static BasicMatchingAlgorithm algorithm;
	private static final Logger LOGGER = Logger.getLogger(IronGpm.class);
	private static final String RULES_FOLDER = "rules";
	/**
	 * Configuration class for the application.
	 */
	private static Properties mConfig;
	private static boolean mIsPolicyPublisherEnabled;

	/**
	 * Nope!
	 */
	private IronGpm() {
	}

	/**
	 * Main class used to start the component.
	 *
	 * @param args
	 *            not used!
	 */
	public static void main(String[] args) {
		LOGGER.info("Starting IronGPM component ...");
		initComponents();

		pulldozer.start();
		LOGGER.info("IronGPM successfully initialized.");
	}

	/**
	 * Initializes the individual software components.
	 */
	private static void initComponents() {
		String config = IronGpm.class.getClassLoader().getResource("irongpm.yml").getPath();
		mConfig = new Properties(config);
		LoggingListener l = new LoggingListener();

		DataReciever.init();
		pulldozer = new Pulldozer();

		IfmapPublishUtil.init();
		pulldozer.addListener(l);

		if (mConfig.getBoolean("irongpm.publisher.selfpublish.enabled", true)) {
			try {
				String ipValue = InetAddress.getLocalHost().getHostAddress();
				String deviceName = mConfig.getString("irongpm.publisher.selfpublish.devicename", "irongpm");
				PublishRequest selfPublishRequest = SelfPublisher.createSelfPublishRequest(ipValue, deviceName);
				IfmapPublishUtil.publish(selfPublishRequest);
			} catch (UnknownHostException e) {
				LOGGER.error("Could not publish self-information: "
						+ e.getMessage());
			}
		}

		algorithm = new BasicMatchingAlgorithm();
		try {
			initializeRules();
		} catch (Exception e) {
			LOGGER.error("Could not load rules ...: "
					+ e.getMessage());
			System.exit(1);
		}
		pulldozer.addListener(algorithm);
	}

	/**
	 * @return the config object for the application
	 */
	public static Properties getConfig() {
		if (mConfig == null) {
			throw new RuntimeException("Application property has not been initialized. This is not good!");
		}
		return mConfig;
	}

	private static void initializeRules() {
		ArrayList<RuleLoader> loaders = new ArrayList<>();
		List<RuleWrapper> rules = new ArrayList<>();
		List<URL> subdirectoryJarFiles = new ArrayList<>();

		File ruleFolder = new File(RULES_FOLDER);
		if (!ruleFolder.exists()) {
			LOGGER.error("Rule Folder was not found!", new FileNotFoundException("Folder '"
					+ RULES_FOLDER
					+ "' was not found. Make sure it exists on the same level as the .jar"));
			System.exit(1);
		}
		subdirectoryJarFiles = FileUtils.listJarFiles(ruleFolder);
		if (subdirectoryJarFiles.size() > 0) {
			ClassLoader loader = URLClassLoader.newInstance(subdirectoryJarFiles.toArray(new URL[] {}));

			for (URL jarFile : subdirectoryJarFiles) {
				RuleLoader ruleLoader = loadRuleLoaderFromJarFile(loader, jarFile);
				if (ruleLoader != null) {
					loaders.add(ruleLoader);
					LOGGER.debug("RuleLoader '"
							+ ruleLoader.getClass().getCanonicalName()
							+ "' was loaded from jar-file '" + FileUtils.getFileName(jarFile) + "'");
				} else {
					LOGGER.warn("Could not instantiate ruleLoader from jar-file '"
							+ FileUtils.getFileName(jarFile)
							+ "'");
				}
			}
		} else {
			LOGGER.warn("Did not find any jar-files for RuleLoaders");
		}

		Map<String, List<RuleWrapper>> ruleLoaderMapping = new HashMap<>();
		RuleWrapper rw;

		if (loaders.size() > 0) {
			for (RuleLoader rl : loaders) {
				List<PatternRule> loadedRules = rl.loadRules();
				List<RuleWrapper> ruleWrappers = new ArrayList<>();
				for (PatternRule rule : loadedRules) {
					rw = new RuleWrapper(rule);
					ruleWrappers.add(rw);
					LOGGER.debug("Rule '"
							+ rule.getName() + "' loaded successfully from: '"
							+ rl.getClass().getSimpleName() + "'");
				}
				rules.addAll(ruleWrappers);
				ruleLoaderMapping.put(rl.getClass().getSimpleName(), ruleWrappers);
			}
		} else {
			LOGGER.warn("Did not find any rule loaders inside '"
					+ RULES_FOLDER);
		}
		if (rules.size() > 0) {
			mIsPolicyPublisherEnabled = mConfig.getBoolean("irongpm.publisher.policy.enabled", false);
			if (mIsPolicyPublisherEnabled) {
				try {
					PolicyPublisher.publishRules(ruleLoaderMapping);
				} catch (IfmapErrorResult | IfmapException e) {
					LOGGER.error("Publishing rules to IF-MAP failed: "
							+ e.getMessage());
					System.exit(1);
				}
			}

			for (RuleWrapper r : rules) {
				while (algorithm.hasRuleId(r.getId())) {
					long newId = r.getId()
							+ 1;
					r.setId(newId);
				}
				algorithm.addRule(r);

			}
		} else {
			LOGGER.warn("Did not find any rules!");
		}
	}

	/**
	 * Try to load a {@link RuleLoader} from a Jar-File with a given {@link ClassLoader} and returns a fresh
	 * instance of that class. If loading fails, <code>null</code> is returned.
	 *
	 * @param classLoader
	 *            a {@link ClassLoader} that contains all needed native libraries and Java dependencies for loading a
	 *            {@link RuleLoader} from the JAR file
	 * @param jarFile
	 *            JAR file to load the {@link RuleLoader} from
	 * @return a instance of {@link RuleLoader}, or null if loading fails
	 */
	private static RuleLoader loadRuleLoaderFromJarFile(ClassLoader classLoader, URL jarFile) {
		try {
			List<String> classNames = ReflectionUtils.getClassNames(jarFile);
			return ReflectionUtils.loadClass(classLoader, classNames, RuleLoader.class);
		} catch (IOException | SecurityException | IllegalArgumentException e) {
			LOGGER.warn("Could not load RuleLoader from "
					+ jarFile + ": " + e);
		}

		return null;
	}

}
