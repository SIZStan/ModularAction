package indi.ma;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class ModularActionsConfig {
	public static class Cooldown {
		public float crawlCooldown;
		public float leanCooldown;
		public float sitCooldown;
		//public float slideCooldown;
		public float chargeCooldown;

		public Cooldown() {
			this.sitCooldown = 0.75f;
			this.crawlCooldown = 0.75f;
			this.leanCooldown = 0.0f;
			//this.slideCooldown=0.75f;
			this.chargeCooldown=0.75f;
		}
	}

	public static class Crawl {
		public float blockAngle;
		public boolean blockView;

		public Crawl() {
			this.blockView = true;
			this.blockAngle = 190.0f;
		}
	}

	public static class Lean {
		public boolean autoHold;
		public boolean mouseCorrection;
		public boolean withGunsOnly;

		public Lean() {
			this.autoHold = false;
			this.mouseCorrection = true;
			this.withGunsOnly = false;
		}
	}

	public static class Sit {
		public boolean autoHold;

		public Sit() {
			this.autoHold = true;
		}
	}

	public static class Slide {
		public boolean enable;
		public float maxForce;

		public Slide() {
			this.enable = true;
			this.maxForce = 1.0f;
		}
	}

	public Cooldown cooldown;
	public Crawl crawl;
	public Lean lean;
	public Sit sit;
	public Slide slide;
	public String version;

	public ModularActionsConfig(File configFile) {
		Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
		try {
			if (configFile.exists()) {
				JsonReader jsonReader = new JsonReader(new FileReader(configFile));
				ModularActionsConfig config = (ModularActionsConfig) gson.fromJson(jsonReader,
						ModularActionsConfig.class);
				//System.out.println("Comparing version " + config.version + " to " + "1.0.0f");
				if (config.version == null) {
					try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), "UTF-8")) {
						gson.toJson(this, writer);
					}
					ModContainer.CONFIG = this;
				} else {
					ModContainer.CONFIG = config;
				}
			} else {
				this.cooldown=new Cooldown();
				this.crawl=new Crawl();
				this.lean=new Lean();
				this.sit=new Sit();
				this.slide=new Slide();
				this.version="1.0.0";
				try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), "UTF-8")) {
					gson.toJson(this, writer);
				}
				ModContainer.CONFIG = this;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
