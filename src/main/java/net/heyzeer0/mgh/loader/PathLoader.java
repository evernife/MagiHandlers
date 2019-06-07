package net.heyzeer0.mgh.loader;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import cpw.mods.fml.relauncher.CoreModManager;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Thomas Vanmellaerts
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class PathLoader {

    private final static Gson gson = new Gson();
    private final static File modFolder = new File("mods");
    private List<String> loadedPatches = new ArrayList<>();

    private Map<String,File> getInstalledMods(){
        Map<String,File> mapOfMods = new HashMap<String,File>();

        for (File aModFile : modFolder.listFiles()) {
            if (aModFile.isDirectory() || !aModFile.getName().endsWith(".jar")){
                continue;
            }
            try {
                ZipFile zipFile = new ZipFile(aModFile);

                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                ZipEntry mcModInfo = null;

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.getName().equals("mcmod.info")) {
                        mcModInfo = entry;
                        break;
                    }
                }

                String modID;
                if (mcModInfo != null) {
                    try {
                        InputStream inputStream = zipFile.getInputStream(mcModInfo);
                        JSONParser jsonParser = new JSONParser();
                        JSONArray jsonArray = (JSONArray) jsonParser.parse(
                                new InputStreamReader(inputStream, "UTF-8"));
                        JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                        modID = ((String) jsonObject.get("modid")).toLowerCase();
                        zipFile.close();
                    }catch (Exception e){
                        modID = null;
                    }
                } else {
                    modID = null;
                }

                if (modID != null){
                    mapOfMods.put(modID,aModFile);
                }
            }catch(Exception ignored){

            }
        }
        return mapOfMods;
    }

    public void loadPatches() throws Exception{
        boolean isThermos = false;
        try {
            Class.forName("thermos.Thermos");
            isThermos = true;
        } catch (Exception e) {}

        Map<String,File> mapOfInstalledMods = getInstalledMods();
        if (!isThermos) {
            MixinEnvironment.getDefaultEnvironment().addConfiguration("mixins/mixin-forge.json");
            Patch[] patches = gson.fromJson(Resources.toString(Resources.getResource("mixin-patches.json"), Charset.forName("UTF-8")), Patch[].class);
            for (Patch patch : patches) {
                File modfile = patch.getModid().isEmpty() ? new File(modFolder, patch.getFile()) : mapOfInstalledMods.get(patch.getModid().toLowerCase());
                if (modfile != null && modfile.exists()) {
                    loadModJar(modfile);
                    MixinEnvironment.getDefaultEnvironment().addConfiguration(patch.getMixin());
                    LogManager.getLogger().info("[MagiHandlers] Applying " + patch.getMixin() + " to " + patch.getName());
                    loadedPatches.add(patch.getName());
                }
            }
        } else {
            Mixins.addConfiguration("mixins/mixin-forge.json");
            Patch[] patches = gson.fromJson(Resources.toString(Resources.getResource("mixin-patches.json"), Charset.forName("UTF-8")), Patch[].class);
            for (Patch patch : patches) {
                File modfile = patch.getModid().isEmpty() ? new File(modFolder, patch.getFile()) : mapOfInstalledMods.get(patch.getModid().toLowerCase());
                if (modfile != null && modfile.exists()) {
                    loadModJar(modfile);
                    Mixins.addConfiguration(patch.getMixin());
                    LogManager.getLogger().info("[MagiHandlers] Applying " + patch.getMixin() + " to " + patch.getName());
                    loadedPatches.add(patch.getName());
                }
            }
        }
    }

    private void loadModJar(File jar) throws Exception{
        ((LaunchClassLoader) this.getClass().getClassLoader()).addURL(jar.toURI().toURL());
        CoreModManager.getReparseableCoremods().add(jar.getName());
    }

    public List<String> getLoadedPatches() {
        return loadedPatches;
    }

}
