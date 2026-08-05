/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.transferapp;

import org.apache.commons.lang.StringUtils;
import org.openmrs.User;
import org.openmrs.api.context.Context;

/**
 * Helpers for privilege checks and user-facing access-denied messages.
 */
public final class TransferPrivilegeHelper {

	private TransferPrivilegeHelper() {
	}

	public static boolean hasPrivilege(String privilege) {
		User user = Context.getAuthenticatedUser();
		return user != null && StringUtils.isNotBlank(privilege) && user.hasPrivilege(privilege);
	}

	public static String requiredPrivilegeMessage(String privilege) {
		if (StringUtils.isBlank(privilege)) {
			return "You do not have permission to access this feature.";
		}
		return "You do not have permission to access this feature. Required privilege: " + privilege;
	}

	public static boolean isPrivilegeException(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			String className = current.getClass().getName();
			if (className.contains("APIAuthenticationException")
					|| className.contains("ContextAuthenticationException")
					|| className.contains("AccessDeniedException")) {
				return true;
			}
			String message = current.getMessage();
			if (message != null) {
				String lower = message.toLowerCase();
				if (lower.contains("privileges required")
						|| lower.contains("privilege required")
						|| lower.contains("user is not logged in")
						|| lower.contains("authentication required")) {
					return true;
				}
			}
			current = current.getCause();
		}
		return false;
	}

	public static String resolveUserFacingMessage(Throwable throwable, String requiredPrivilege, String fallback) {
		if (isPrivilegeException(throwable)) {
			String extracted = extractRequiredPrivilege(throwable);
			if (StringUtils.isNotBlank(extracted)) {
				return requiredPrivilegeMessage(extracted);
			}
			if (StringUtils.isNotBlank(requiredPrivilege)) {
				return requiredPrivilegeMessage(requiredPrivilege);
			}
			return "You do not have permission to access this feature.";
		}

		Throwable current = throwable;
		while (current != null) {
			if (StringUtils.isNotBlank(current.getMessage())) {
				return current.getMessage().trim();
			}
			current = current.getCause();
		}
		return fallback != null ? fallback : "An unexpected error occurred";
	}

	private static String extractRequiredPrivilege(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			String message = current.getMessage();
			if (message != null) {
				String marker = "Privileges required:";
				int idx = message.indexOf(marker);
				if (idx < 0) {
					idx = message.toLowerCase().indexOf("privileges required:");
					if (idx >= 0) {
						marker = message.substring(idx, idx + "privileges required:".length());
					}
				}
				if (idx >= 0) {
					String remainder = message.substring(idx + marker.length()).trim();
					if (remainder.length() > 0) {
						int end = remainder.indexOf('.');
						if (end > 0) {
							remainder = remainder.substring(0, end).trim();
						}
						return remainder;
					}
				}
			}
			current = current.getCause();
		}
		return null;
	}

}
