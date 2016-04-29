/*
 * IP Monitor is a simple application which monitors your public IP
 * address for changes and lets you set different kinds of notification
 * such as email, audio, pop up or executing a command. It can also run
 * in background as a Windows service or Linux/Mac daemon.
 *
 * Copyright (C) 2007 - 2016  Gabriel Zanetti http://github.com/pupi1985
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package model.service;

import java.io.IOException;
import java.util.Arrays;

public class WindowsBasedService extends AbstractService {

	public String getServiceName() {
		return "Service";
	}

	protected String getScriptFileName() {
		return "IPMonitor.bat";
	}

	public boolean isRunning() throws IOException {
		return this.runScriptWithArguments(Arrays.asList("status")).getOutput().toLowerCase().contains("Running: Yes");
	}

	public boolean shouldIncludeExitCode() {
		return false;
	}

}