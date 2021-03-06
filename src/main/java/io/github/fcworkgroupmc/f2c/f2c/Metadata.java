/*
 *  Copyright (C) 2020  FCWorkgroupMC
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.fcworkgroupmc.f2c.f2c;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.INameMappingService;
import io.github.fcworkgroupmc.f2c.f2c.transformationservices.NothingModLocator;
import net.minecraftforge.fml.loading.FMLCommonLaunchHandler;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class Metadata {
	public static Proxy proxy;
	public static String mcVersion;
	public static final URL location = Metadata.class.getProtectionDomain().getCodeSource().getLocation();
	public static final String FABRIC_MOD_SUFFIX = ".fabricmod";
	public static final String JAR_SUFFIX = ".jar";
	/** fabric mod definition(fabric.mod.json) */
	public static final String FABRIC_MOD_DEF = "fabric.mod.json";
	public static final String F2C_DIR = ".f2c";

	public static final IModLocator nothingLocator = new NothingModLocator();

	public static final boolean DEV = true;
	public static boolean isDevelopment() {
		return location == null || !location.getPath().endsWith(".jar");
	}
	public static boolean isNotDev() {
		return location != null && location.getPath().endsWith(".jar");
	}

	public static boolean funcReady;
	public static BiFunction<INameMappingService.Domain, String, String> remapFunc;
	public static void funcReady() {
		funcReady = true;
		remapFunc = Launcher.INSTANCE.environment().findNameMapping("intermediary").get();
	}

	private static final Method addLibraries = ((Supplier<Method>) () -> {
		try {
			final Method addLibraries = FMLCommonLaunchHandler.class.getDeclaredMethod("addLibraries", List.class);
			addLibraries.setAccessible(true);
			return addLibraries;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}).get();
	public static void addLibraries(List<ModFile> libs) {
		try {
			final String launchTarget = Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.LAUNCHTARGET.get()).orElse("MISSING");
			final FMLCommonLaunchHandler launchHandler = (FMLCommonLaunchHandler) Launcher.INSTANCE.environment().findLaunchHandler(launchTarget).get();
			addLibraries.invoke(launchHandler, libs);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	public static void addLibrary(ModFile libs) {
		addLibraries(Collections.singletonList(libs));
	}

	public static boolean disableFabricLoader;

	private static final Logger LOGGER = LogManager.getLogger();
	public static void initMcVersion() {
		try {
			Field mcVersionF = FMLLoader.class.getDeclaredField("mcVersion");
			mcVersionF.setAccessible(true);
			mcVersion = (String) mcVersionF.get(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			LOGGER.fatal("Error when getting minecraft version", e);
		}
	}
	static {
		String host = System.getProperty("f2c.proxyHost");
		String port = System.getProperty("f2c.proxyPort");
		proxy = port == null ? Proxy.NO_PROXY : new Proxy(Proxy.Type.HTTP,
				host == null ? new InetSocketAddress(Integer.parseInt(port)) : new InetSocketAddress(host, Integer.parseInt(port)));
	}
}