package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import net.minecraft.client.Minecraft;

public class PlayerHelper {
	public Minecraft mc;
	public EntityPlayer ep;
	public static HashMap waypoints;
	public HashMap commands;
	public static final String[] cmd = new String[]{"ascend", "clear", "damage", "defuse", "descend", "destroy", "diff", "difficulty", "dupe", "duplicate", "dropstore", "explode", "ext", "extinguish", "falldamage", "firedamage", "give", "goto", "grow", "h", "heal", "health", "help", "home", "i", "instantmine", "item", "itemname", "itemstack", "jump", "kill", "killnpc", "l", "listwaypoints", "msg", "music", "p", "pos", "rem", "removedrops", "repair", "replenish", "return", "s", "search", "set", "setspawn", "setspeed", "setjump", "spawn", "spawnstack", "t", "tele", "time", "timeschedule", "useportal", "waterdamage", "/box", "/contract", "/copy", "/expand", "/fill", "/help", "/load", "/paste", "/remove", "/reset", "/save", "/set1", "/set2", "/walls"};
	private HashMap spawnlist;
	private HashMap spawnidlist;
	private List spawnignore;
	private double prevx;
	private double prevy;
	private double prevz;
	public boolean instant = false;
	public boolean falldamage = true;
	public boolean waterdamage = true;
	public boolean damage = true;
	private int[] cubeposx = new int[2];
	private int[] cubeposy = new int[2];
	private int[] cubeposz = new int[2];
	public double gravity;
	public float speed;
	public int[][][] clipboard;
	public int[] timeschedule;
	public int lastrift;
	public Vector history;
	public static Vector itemnames;
	public static String ERRMSG_PARAM = "Invalid number of parameters.";
	public static String ERRMSG_PARSE = "Could not parse input.";
	public static String ERRMSG_NOTSET = "WorldEdit points not set.";

	public PlayerHelper(Minecraft mc, EntityPlayer ep) {
		this.mc = mc;
		this.ep = ep;
		waypoints = new HashMap();
		this.history = new Vector();
		this.loadConfig();
		this.loadSettings();
		this.populateItemNames();
	}

	public void loadConfig() {
		Minecraft minecraft10001 = this.mc;
		this.loadConfig(Minecraft.getMinecraftDir());
	}

	public void loadConfig(File f) {
		File config = new File(f, "mods/sppcommands/sppcommands.properties");
		this.commands = new HashMap();
		Properties p = null;

		try {
			(p = new Properties()).load(new FileInputStream(config));
		} catch (Exception exception5) {
		}

		for(int i = 0; i < cmd.length; ++i) {
			this.commands.put(this.readKey(p, cmd[i]), cmd[i]);
		}

	}

	public String readKey(Properties p, String key) {
		return this.readKey(p, key, key);
	}

	public String readKey(Properties p, String key, String d) {
		if(p == null) {
			return d;
		} else {
			String value = p.getProperty(key);
			return value != null && !value.trim().equalsIgnoreCase("") ? value.trim() : d;
		}
	}

	public void loadSettings() {
	}

	public void loadSettings(File f) {
		File settings = new File(f, "spc.settings");
		Properties p = null;

		try {
			(p = new Properties()).load(new FileInputStream(settings));
		} catch (Exception exception5) {
		}

		this.instant = Boolean.parseBoolean(this.readKey(p, "instant", "false"));
		this.prevx = Double.parseDouble(this.readKey(p, "previousx", "0"));
		this.prevy = Double.parseDouble(this.readKey(p, "previousy", "0"));
		this.prevz = Double.parseDouble(this.readKey(p, "previousz", "0"));
		this.gravity = Double.parseDouble(this.readKey(p, "gravity", "1"));
		this.speed = Float.parseFloat(this.readKey(p, "speed", "1"));
		this.falldamage = Boolean.parseBoolean(this.readKey(p, "falldamage", "true"));
		this.waterdamage = Boolean.parseBoolean(this.readKey(p, "waterdamage", "true"));
		this.ep.isImmuneToFire = Boolean.parseBoolean(this.readKey(p, "firedamage", "false"));
		this.damage = Boolean.parseBoolean(this.readKey(p, "damage", "true"));
		this.lastrift = Integer.parseInt(this.readKey(p, "lastrift", "-1"));

		int i;
		for(i = 0; i < this.cubeposx.length; ++i) {
			this.cubeposx[i] = Integer.parseInt(this.readKey(p, "cubeposx" + i, "0"));
			this.cubeposy[i] = Integer.parseInt(this.readKey(p, "cubeposy" + i, "0"));
			this.cubeposz[i] = Integer.parseInt(this.readKey(p, "cubeposz" + i, "0"));
		}

		if(this.lastrift > -1) {
			this.timeschedule = new int[4];

			for(i = 0; i < this.timeschedule.length; ++i) {
				this.timeschedule[i] = Integer.parseInt(this.readKey(p, "timeschedule" + i, "0"));
			}
		}

	}

	public void saveSettings() {
		this.saveSettings(this.mc.theWorld.field_9432_t);
	}

	public void saveSettings(File f) {
		File settings = new File(f, "spc.settings");
		Properties p = new Properties();
		p.setProperty("instant", (new Boolean(this.instant)).toString());
		p.setProperty("previousx", (new Double(this.prevx)).toString());
		p.setProperty("previousy", (new Double(this.prevy)).toString());
		p.setProperty("previousz", (new Double(this.prevz)).toString());
		p.setProperty("gravity", (new Double(this.gravity)).toString());
		p.setProperty("speed", (new Float(this.speed)).toString());
		p.setProperty("falldamage", (new Boolean(this.falldamage)).toString());
		p.setProperty("waterdamage", (new Boolean(this.waterdamage)).toString());
		p.setProperty("firedamage", (new Boolean(this.ep.isImmuneToFire)).toString());
		p.setProperty("damage", (new Boolean(this.damage)).toString());
		p.setProperty("lastrift", (new Integer(this.lastrift)).toString());

		int e;
		for(e = 0; e < this.cubeposx.length; ++e) {
			p.setProperty("cubeposx" + e, (new Integer(this.cubeposx[e])).toString());
			p.setProperty("cubeposy" + e, (new Integer(this.cubeposy[e])).toString());
			p.setProperty("cubeposz" + e, (new Integer(this.cubeposz[e])).toString());
		}

		if(this.timeschedule != null) {
			for(e = 0; e < 4; ++e) {
				p.setProperty("timeschedule" + e, (new Integer(this.timeschedule[e])).toString());
			}
		}

		try {
			p.store(new FileOutputStream(settings), "Single Player Commands - Settings");
		} catch (Exception exception5) {
		}

	}

	public void readWaypointsFromNBT(File f) {
		File wp = new File(f, "waypoints.dat");
		if(wp.exists()) {
			waypoints.clear();

			NBTTagCompound nbttagcompound1;
			try {
				nbttagcompound1 = CompressedStreamTools.func_1138_a(new FileInputStream(wp));
			} catch (Exception exception14) {
				return;
			}

			NBTTagList nbttaglist = nbttagcompound1.getTagList("waypoints");

			for(int i = 0; i < nbttaglist.tagCount(); ++i) {
				NBTTagCompound nbttagcompound = (NBTTagCompound)nbttaglist.tagAt(i);
				String s = nbttagcompound.getString("Name");
				double d = nbttagcompound.getDouble("X");
				double d1 = nbttagcompound.getDouble("Y");
				double d2 = nbttagcompound.getDouble("Z");
				waypoints.put(s, new double[]{d, d1, d2});
			}

		}
	}

	public void writeWaypointsToNBT(File f) {
		if(waypoints.size() != 0) {
			NBTTagList nbttaglist = new NBTTagList();
			Iterator nbttagcompound1 = waypoints.keySet().iterator();

			while(nbttagcompound1.hasNext()) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				String wpnew = (String)nbttagcompound1.next();
				nbttagcompound.setString("Name", wpnew);
				nbttagcompound.setDouble("X", ((double[])((double[])waypoints.get(wpnew)))[0]);
				nbttagcompound.setDouble("Y", ((double[])((double[])waypoints.get(wpnew)))[1]);
				nbttagcompound.setDouble("Z", ((double[])((double[])waypoints.get(wpnew)))[2]);
				nbttaglist.setTag(nbttagcompound);
			}

			NBTTagCompound nbttagcompound11 = new NBTTagCompound();
			nbttagcompound11.setTag("waypoints", nbttaglist);
			File wpnew1 = new File(f, "waypoints.dat_new");
			File wpold = new File(f, "waypoints.dat_old");
			File wp = new File(f, "waypoints.dat");

			try {
				CompressedStreamTools.writeGzippedCompoundToOutputStream(nbttagcompound11, new FileOutputStream(wpnew1));
				if(wpold.exists()) {
					wpold.delete();
				}

				wp.renameTo(wpold);
				if(wp.exists()) {
					wp.delete();
				}

				wpnew1.renameTo(wp);
				if(wpnew1.exists()) {
					wpnew1.delete();
				}
			} catch (Exception exception9) {
			}

		}
	}

	public Map getSpawnList() {
		if(this.spawnlist != null) {
			return this.spawnlist;
		} else {
			this.setSpawnLists();
			return this.spawnlist;
		}
	}

	public Map getSpawnIdList() {
		if(this.spawnidlist != null) {
			return this.spawnidlist;
		} else {
			this.setSpawnLists();
			return this.spawnidlist;
		}
	}

	private void setSpawnLists() {
		try {
			Field[] i = EntityList.class.getDeclaredFields();

			for(int temp = 0; temp < i.length; ++temp) {
				i[temp].setAccessible(true);
				if(Modifier.isStatic(i[temp].getModifiers())) {
					Object o = i[temp].get((Object)null);
					if(o instanceof Map) {
						Map temp1 = (Map)o;

						try {
							if(temp1.keySet().iterator().next() instanceof String) {
								this.spawnlist = (HashMap)temp1;
							} else if(temp1.keySet().iterator().next() instanceof Integer) {
								this.spawnidlist = (HashMap)temp1;
							}
						} catch (Exception exception6) {
						}
					}
				}
			}
		} catch (Exception exception7) {
			this.spawnlist = new HashMap();
			this.spawnidlist = new HashMap();
			return;
		}

		this.spawnignore = new Vector();
		Iterator iterator8 = this.spawnlist.values().iterator();

		while(iterator8.hasNext()) {
			Class class9 = (Class)iterator8.next();
			if(!EntityLiving.class.isAssignableFrom(class9)) {
				this.spawnignore.add(class9);
			}
		}

	}

	public String findNPC(Integer i) {
		Class c = (Class)this.getSpawnIdList().get(i);
		if(c != null && !this.spawnignore.contains(c)) {
			Iterator it = this.getSpawnList().keySet().iterator();

			String tmp;
			do {
				if(!it.hasNext()) {
					return null;
				}

				tmp = (String)it.next();
			} while(this.getSpawnList().get(tmp) != c);

			return tmp;
		} else {
			return null;
		}
	}

	public Class getNPC(String name) {
		Class creature = null;
		Iterator t = this.getSpawnList().keySet().iterator();

		while(t.hasNext()) {
			String key = (String)t.next();
			if(name.equalsIgnoreCase(key)) {
				creature = (Class)this.getSpawnList().get(key);
				break;
			}
		}

		return creature;
	}

	public void setCurrentPosition() {
		this.prevx = this.ep.posX;
		this.prevy = this.ep.posY;
		this.prevz = this.ep.posZ;
	}

	public void processCommand(String s) {
		try {
			if(s.startsWith(".")) {
				s = s.substring(1);
			}

			if(s.startsWith(".")) {
				this.processWorldEdit(s);
			} else {
				this.processCommands(s);
			}
		} catch (Exception exception6) {
			Exception e = exception6;
			this.sendError("UNHANDLED COMMANDS EXCEPTION - ");
			this.sendError(exception6.getMessage());
			exception6.printStackTrace();

			try {
				Minecraft minecraft10002 = this.mc;
				File ex = new File(Minecraft.getMinecraftDir(), "sppexception-" + (new Date()).getTime() + ".log");
				PrintWriter p = new PrintWriter(new FileOutputStream(ex));
				e.printStackTrace(p);
				p.println();
				p.println("Command = " + s);
				p.close();
				this.sendMessage("Error log written at: " + ex.getAbsolutePath());
			} catch (Exception exception5) {
				this.sendError("Could not write error log.");
			}

		}
	}

	public void processCommands(String s) throws Exception {
		String[] split = s.trim().split(" ");
		split[0] = (String)this.commands.get(split[0]);
		if(split[0] == null) {
			this.sendError("Command does not exist.");
		} else {
			Item distance;
			int npcs;
			String e;
			int sml;
			if(!split[0].equalsIgnoreCase("item") && !split[0].equalsIgnoreCase("i") && !split[0].equalsIgnoreCase("give")) {
				double i;
				double d33;
				double d42;
				if(!split[0].equalsIgnoreCase("tele") && !split[0].equalsIgnoreCase("t")) {
					if(!split[0].equalsIgnoreCase("pos") && !split[0].equalsIgnoreCase("p")) {
						int i34;
						int i35;
						if(split[0].equalsIgnoreCase("setspawn")) {
							i34 = this.mc.theWorld.spawnX;
							npcs = this.mc.theWorld.spawnY;
							i35 = this.mc.theWorld.spawnZ;
							if(split.length == 1) {
								i34 = (int)this.ep.posX;
								npcs = (int)this.ep.posY;
								i35 = (int)this.ep.posZ;
							} else {
								if(split.length != 4) {
									this.sendError(ERRMSG_PARAM);
									return;
								}

								try {
									i34 = (int)Double.parseDouble(split[1]);
									npcs = (int)Double.parseDouble(split[2]);
									i35 = (int)Double.parseDouble(split[3]);
								} catch (Exception exception26) {
									this.sendError(ERRMSG_PARSE);
									return;
								}
							}

							this.mc.theWorld.spawnX = i34;
							this.mc.theWorld.spawnY = npcs;
							this.mc.theWorld.spawnZ = i35;
							this.sendMessage("Spawn set at (" + i34 + "," + npcs + "," + i35 + ")");
						} else {
							String string36;
							if(!split[0].equalsIgnoreCase("set") && !split[0].equalsIgnoreCase("s")) {
								String string37;
								if(split[0].equalsIgnoreCase("goto")) {
									if(split.length < 2) {
										this.sendError(ERRMSG_PARAM);
										return;
									}

									string36 = s.substring(5).trim();
									if(waypoints.containsKey(string36)) {
										double[] d39 = (double[])waypoints.get(string36);
										string37 = this.positionAsString();
										this.setCurrentPosition();
										this.ep.setPosition(d39[0], d39[1], d39[2]);
										this.sendMessage("Moved from: " + string37 + " to: " + this.positionAsString());
									} else {
										this.sendError("Could not find specified waypoint.");
									}
								} else if(split[0].equalsIgnoreCase("rem")) {
									if(split.length < 2) {
										this.sendError(ERRMSG_PARAM);
										return;
									}

									string36 = s.substring(4).trim();
									if(waypoints.containsKey(string36)) {
										waypoints.remove(string36);
										this.writeWaypointsToNBT(this.mc.theWorld.field_9432_t);
										this.sendMessage("Waypoint \"" + string36 + "\" removed.");
									} else {
										this.sendError("Could not find specified waypoint.");
									}
								} else if(split[0].equalsIgnoreCase("home")) {
									this.setCurrentPosition();
									this.ep.setLocationAndAngles((double)this.mc.theWorld.spawnX + 0.5D, (double)(this.mc.theWorld.spawnY + 1), (double)this.mc.theWorld.spawnZ + 0.5D, 0.0F, 0.0F);
									this.ep.preparePlayerToSpawn();
								} else if(split[0].equalsIgnoreCase("kill")) {
									this.ep.canAttackEntity(this.ep, Integer.MAX_VALUE);
								} else {
									int i38;
									Iterator iterator44;
									if(split[0].equalsIgnoreCase("listwaypoints") || split[0].equalsIgnoreCase("l")) {
										i34 = waypoints.size();
										if(i34 == 0) {
											this.sendMessage("No waypoints found.");
											return;
										}

										iterator44 = waypoints.keySet().iterator();
										String[] string64 = new String[5];
										i38 = 0;

										while(iterator44.hasNext()) {
											if(string64[i38] == null) {
												string64[i38] = "";
											}

											String string63 = (String)iterator44.next();
											if((string63 + string64[i38]).length() > 98 && i38 >= string64.length) {
												break;
											}

											if((string63 + string64[i38]).length() > 98) {
												++i38;
												string64[i38] = string63;
											} else {
												string64[i38] = string64[i38] + string63 + ", ";
											}
										}

										this.sendMessage("Waypoints (" + i34 + "): ");

										for(sml = 0; sml < string64.length && string64[sml] != null; ++sml) {
											this.sendMessage(string64[sml]);
										}
									} else if(split[0].equalsIgnoreCase("clear")) {
										for(i34 = 0; i34 < 50; ++i34) {
											this.sendMessage("");
										}
									} else if(split[0].equalsIgnoreCase("time")) {
										if(s.trim().length() == 4 || s.trim().length() == 8 && s.trim().startsWith("time get")) {
											this.printCurrentTime();
										}

										if(split.length > 2) {
											byte b40 = -1;
											if(split[2].equalsIgnoreCase("day")) {
												b40 = 2;
											} else if(split[2].equalsIgnoreCase("hour")) {
												b40 = 1;
											} else if(split[2].equalsIgnoreCase("minute")) {
												b40 = 0;
											}

											if(b40 == -1) {
												this.sendError("Invalid time command: " + split[2]);
												return;
											}

											npcs = this.getTime()[2];
											i35 = this.getTime()[0];
											i38 = this.getTime()[1];
											if(split[1].equalsIgnoreCase("get")) {
												this.sendMessage(split[2].toUpperCase() + ": " + this.getTime()[b40]);
											} else if(split[1].equalsIgnoreCase("set") && split.length > 3) {
												boolean z45 = true;

												try {
													sml = Integer.parseInt(split[3]);
												} catch (Exception exception25) {
													this.sendError(ERRMSG_PARSE);
													return;
												}

												if(sml < 0) {
													return;
												}

												if(b40 == 0) {
													sml = (int)((double)(sml % 60) / 60.0D * 1000.0D);
													this.mc.theWorld.worldTime = (long)(npcs * 24000 + i38 * 1000 + sml);
												} else if(b40 == 1) {
													sml = sml % 24 * 1000;
													this.mc.theWorld.worldTime = (long)((double)(npcs * 24000 + sml) + (double)i35 / 60.0D * 1000.0D);
												} else {
													if(b40 != 2) {
														this.sendError("Invalid time command: " + split[2]);
														return;
													}

													sml *= 24000;
													this.mc.theWorld.worldTime = (long)((double)(sml + i38 * 1000) + (double)i35 / 60.0D * 1000.0D);
												}

												this.printCurrentTime();
											}
										} else if(split.length == 2) {
											i34 = this.getTime()[2];
											if(split[1].equalsIgnoreCase("day")) {
												this.mc.theWorld.worldTime = (long)((i34 + 1) * 24000);
												this.printCurrentTime();
											} else if(split[1].equalsIgnoreCase("night")) {
												this.mc.theWorld.worldTime = (long)(i34 * 24000 + 13000);
												this.printCurrentTime();
											}
										}
									} else if(split[0].equalsIgnoreCase("health")) {
										if(split.length < 2) {
											this.sendError(ERRMSG_PARAM);
											return;
										}

										if(split[1].equalsIgnoreCase("max")) {
											this.ep.health = 20;
										} else if(split[1].equalsIgnoreCase("min")) {
											this.ep.health = 1;
										} else {
											if(!split[1].equalsIgnoreCase("infinite") && !split[1].equalsIgnoreCase("inf")) {
												this.sendError("Invalid health command: " + split[1]);
												return;
											}

											this.ep.health = 32767;
										}

										this.sendMessage("Health set at " + split[1] + " (" + this.ep.health + ")");
									} else if(split[0].equalsIgnoreCase("heal")) {
										if(split.length < 2) {
											this.sendError(ERRMSG_PARAM);
											return;
										}

										try {
											this.ep.heal(Integer.parseInt(split[1]));
										} catch (Exception exception24) {
											this.sendError(ERRMSG_PARSE);
											return;
										}

										this.sendMessage("Player healed");
									} else {
										Integer integer43;
										if(split[0].equalsIgnoreCase("spawnstack")) {
											if(split.length < 2) {
												this.sendError(ERRMSG_PARAM);
												return;
											}

											if(split[1].equalsIgnoreCase("list")) {
												string36 = "";
												iterator44 = this.getSpawnIdList().keySet().iterator();

												while(iterator44.hasNext()) {
													integer43 = (Integer)iterator44.next();
													e = this.findNPC(integer43);
													if(e != null) {
														string36 = string36 + e + " (" + integer43 + "), ";
													}
												}

												this.sendMessage(string36);
												return;
											}

											distance = null;
											EntityLiving entityLiving41 = null;
											i35 = 1;
											e = "";

											do {
												EntityLiving entityLiving53;
												try {
													Class class52 = null;
													if(!split[i35].equalsIgnoreCase("random") && !split[i35].equalsIgnoreCase("r")) {
														try {
															Integer integer46 = new Integer(split[i35]);
															class52 = (Class)this.getSpawnIdList().get(integer46);
														} catch (Exception exception23) {
															class52 = this.getNPC(split[i35]);
														}
													} else {
														Object[] big = this.getSpawnIdList().values().toArray();
														class52 = (Class)big[(new Random()).nextInt(big.length)];
													}

													if(class52 == null || this.spawnignore.contains(class52)) {
														if(class52 == null) {
															e = e + split[i35] + ", ";
														}
														continue;
													}

													entityLiving53 = (EntityLiving)class52.getConstructor(new Class[]{World.class}).newInstance(new Object[]{this.mc.theWorld});
												} catch (Exception exception30) {
													continue;
												}

												entityLiving53.setLocationAndAngles(this.ep.posX + 3.0D, this.ep.posY, this.ep.posZ + 3.0D, this.ep.rotationYaw, 0.0F);
												this.mc.theWorld.entityJoinedWorld(entityLiving53);
												if(entityLiving41 != null) {
													entityLiving53.mountEntity(entityLiving41);
												}

												entityLiving41 = entityLiving53;
											} while(i35++ < split.length - 1);

											if(!e.equalsIgnoreCase("")) {
												this.sendError("Could not find: " + e);
											}
										} else if(split[0].equalsIgnoreCase("spawn")) {
											if(split.length < 2) {
												this.sendError(ERRMSG_PARAM);
												return;
											}

											if(split[1].equalsIgnoreCase("list")) {
												string36 = "";
												iterator44 = this.getSpawnIdList().keySet().iterator();

												while(iterator44.hasNext()) {
													integer43 = (Integer)iterator44.next();
													e = this.findNPC(integer43);
													if(e != null) {
														string36 = string36 + e + " (" + integer43 + "), ";
													}
												}

												this.sendMessage(string36);
												return;
											}

											distance = null;
											npcs = 1;
											if(split.length > 2) {
												try {
													npcs = Integer.parseInt(split[2]);
												} catch (Exception exception22) {
													npcs = 1;
												}
											}

											Class class58;
											try {
												integer43 = new Integer(split[1]);
												class58 = (Class)this.getSpawnIdList().get(integer43);
											} catch (Exception exception21) {
												class58 = this.getNPC(split[1]);
											}

											try {
												Random random47 = new Random();

												for(i38 = 0; i38 < npcs; ++i38) {
													if(split[1].equalsIgnoreCase("random") || split[1].equalsIgnoreCase("r")) {
														Object[] object59 = this.getSpawnIdList().values().toArray();
														class58 = (Class)object59[(new Random()).nextInt(object59.length)];
													}

													if(class58 != null && !this.spawnignore.contains(class58)) {
														EntityLiving entityLiving60 = (EntityLiving)class58.getConstructor(new Class[]{World.class}).newInstance(new Object[]{this.mc.theWorld});
														entityLiving60.setLocationAndAngles(this.ep.posX + (double)random47.nextInt(5), this.ep.posY, this.ep.posZ + (double)random47.nextInt(5), this.ep.rotationYaw, 0.0F);
														this.mc.theWorld.entityJoinedWorld(entityLiving60);
													}
												}
											} catch (Exception exception32) {
												this.sendError("");
												return;
											}
										} else if(split[0].equalsIgnoreCase("music")) {
											if(split.length < 2) {
												this.mc.sndManager.func_4033_c();
												return;
											}

											try {
												i34 = Integer.parseInt(split[1]);
												if(i34 < 0) {
													i34 = 0;
												} else if(i34 > 100) {
													i34 = 100;
												}

												this.mc.gameSettings.musicVolume = (float)i34 / 100.0F;
												this.mc.sndManager.onSoundOptionsChanged();
											} catch (Exception exception20) {
												this.sendError(ERRMSG_PARSE);
												return;
											}
										} else if(!split[0].equalsIgnoreCase("difficulty") && !split[0].equalsIgnoreCase("diff")) {
											Entity entity49;
											List list55;
											short s65;
											if(split[0].equalsIgnoreCase("killnpc")) {
												s65 = 16;
												if(split.length > 1 && split[1].equalsIgnoreCase("all")) {
													s65 = 128;
												}

												list55 = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(this.ep, AxisAlignedBB.getBoundingBox(this.ep.posX - (double)s65, this.ep.posY - (double)s65, this.ep.posZ - (double)s65, this.ep.posX + (double)s65, this.ep.posY + (double)s65, this.ep.posZ + (double)s65));

												for(i35 = 0; i35 < list55.size(); ++i35) {
													entity49 = (Entity)list55.get(i35);
													if(entity49 instanceof EntityLiving) {
														((EntityLiving)entity49).damageEntity(Integer.MAX_VALUE);
													}
												}
											} else if(!split[0].equalsIgnoreCase("ascend") && !split[0].equalsIgnoreCase("descend")) {
												if(split[0].equalsIgnoreCase("repair")) {
													if(split.length > 1 && split[1].equalsIgnoreCase("all")) {
														for(i34 = 0; i34 < this.ep.inventory.mainInventory.length; ++i34) {
															if(this.ep.inventory.mainInventory[i34] != null) {
																this.ep.inventory.mainInventory[i34].itemDamage = 0;
															}
														}

														for(i34 = 0; i34 < this.ep.inventory.armorInventory.length; ++i34) {
															if(this.ep.inventory.armorInventory[i34] != null) {
																this.ep.inventory.armorInventory[i34].itemDamage = 0;
															}
														}

														return;
													}

													if(this.ep.inventory.getCurrentItem() != null) {
														this.ep.inventory.getCurrentItem().itemDamage = 0;
													}
												} else if(!split[0].equalsIgnoreCase("duplicate") && !split[0].equalsIgnoreCase("dupe")) {
													if(split[0].equalsIgnoreCase("destroy")) {
														if(split.length > 1 && split[1].equalsIgnoreCase("all")) {
															for(i34 = 0; i34 < this.ep.inventory.mainInventory.length; ++i34) {
																this.ep.inventory.mainInventory[i34] = null;
															}

															for(i34 = 0; i34 < this.ep.inventory.armorInventory.length; ++i34) {
																this.ep.inventory.armorInventory[i34] = null;
															}

															return;
														}

														this.ep.inventory.mainInventory[this.ep.inventory.currentItem] = null;
													} else if(split[0].equalsIgnoreCase("itemstack")) {
														if(split.length < 2) {
															this.sendError(ERRMSG_PARAM);
															return;
														}

														try {
															i34 = Integer.parseInt(split[1]);
															npcs = 1;
															if(split.length > 2) {
																npcs = Integer.parseInt(split[2]);
															}

															npcs = npcs > 256 ? 256 : npcs;
															Item item56 = Item.itemsList[i34];
															if(item56 == null) {
																this.sendError("Could not find the specified item");
																return;
															}

															for(i38 = 0; i38 < npcs; ++i38) {
																this.ep.dropPlayerItem(new ItemStack(item56, item56.getItemStackLimit()));
															}
														} catch (Exception exception31) {
															this.sendError(ERRMSG_PARSE);
															return;
														}
													} else if(split[0].equalsIgnoreCase("defuse")) {
														s65 = 16;
														if(split.length > 1 && split[1].equalsIgnoreCase("all")) {
															s65 = 128;
														}

														list55 = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(this.ep, AxisAlignedBB.getBoundingBox(this.ep.posX - (double)s65, this.ep.posY - (double)s65, this.ep.posZ - (double)s65, this.ep.posX + (double)s65, this.ep.posY + (double)s65, this.ep.posZ + (double)s65));

														for(i35 = 0; i35 < list55.size(); ++i35) {
															entity49 = (Entity)list55.get(i35);
															if(entity49 instanceof EntityTNTPrimed) {
																EntityItem entityItem61 = new EntityItem(this.mc.theWorld, entity49.posX, entity49.posY, entity49.posZ, new ItemStack(Item.itemsList[46], 1));
																this.mc.theWorld.setEntityDead(entity49);
																this.mc.theWorld.entityJoinedWorld(entityItem61);
															}
														}
													} else if(split[0].equalsIgnoreCase("jump")) {
														try {
															MovingObjectPosition movingObjectPosition68 = this.ep.rayTrace(1024.0D, 1.0F);
															this.ep.setPosition(movingObjectPosition68.hitVec.xCoord, movingObjectPosition68.hitVec.yCoord, movingObjectPosition68.hitVec.zCoord);
														} catch (Exception exception18) {
															this.sendError("Unknown problem");
															return;
														}
													} else if(split[0].equalsIgnoreCase("return")) {
														if(this.prevy <= 0.0D) {
															return;
														}

														d33 = this.prevx;
														i = this.prevy;
														d42 = this.prevz;
														this.setCurrentPosition();
														this.ep.setPosition(d33, i, d42);
														this.saveSettings(this.mc.theWorld.field_9432_t);
													} else if(split[0].equalsIgnoreCase("instantmine")) {
														this.instant = !this.instant;
														this.sendMessage("Instant mine now " + (this.instant ? "on" : "off"));
														this.saveSettings();
													} else if(!split[0].equalsIgnoreCase("cuboid")) {
														if(split[0].equalsIgnoreCase("setjump")) {
															if(split.length < 2) {
																this.sendError(ERRMSG_PARAM);
																return;
															}

															if(split[1].equalsIgnoreCase("reset")) {
																this.gravity = 1.0D;
																this.falldamage = true;
															} else {
																try {
																	d33 = Double.parseDouble(split[1]);
																	this.gravity = d33 > 1.0D ? d33 : 1.0D;
																	this.falldamage = this.gravity <= 1.0D;
																} catch (Exception exception17) {
																	this.sendError(ERRMSG_PARSE);
																	return;
																}
															}

															this.sendMessage("Player jump set at: " + this.gravity);
															this.saveSettings();
														} else {
															float f69;
															if(split[0].equalsIgnoreCase("setspeed")) {
																if(split.length < 2) {
																	this.sendError(ERRMSG_PARAM);
																	return;
																}

																if(split[1].equalsIgnoreCase("reset")) {
																	this.speed = 1.0F;
																} else {
																	try {
																		f69 = Float.parseFloat(split[1]);
																		this.speed = f69 > 1.0F ? f69 : 1.0F;
																	} catch (Exception exception16) {
																		this.sendError(ERRMSG_PARSE);
																		return;
																	}
																}

																this.sendMessage("Player speed set at: " + this.speed);
																this.saveSettings();
															} else if(split[0].equalsIgnoreCase("falldamage")) {
																this.falldamage = !this.falldamage;
																this.sendMessage("Fall damage now " + (this.falldamage ? "on" : "off"));
																this.saveSettings();
															} else if(split[0].equalsIgnoreCase("waterdamage")) {
																this.waterdamage = !this.waterdamage;
																this.sendMessage("Water damage now " + (this.waterdamage ? "on" : "off"));
																this.saveSettings();
															} else if(split[0].equalsIgnoreCase("firedamage")) {
																this.ep.isImmuneToFire = !this.ep.isImmuneToFire;
																this.sendMessage("Fire damage now " + (this.ep.isImmuneToFire ? "off" : "on"));
																this.saveSettings();
															} else if(split[0].equalsIgnoreCase("damage")) {
																this.damage = !this.damage;
																this.sendMessage("Damage now " + (this.damage ? "on" : "off"));
																this.saveSettings();
															} else if(!split[0].equalsIgnoreCase("ext") && !split[0].equalsIgnoreCase("extinguish")) {
																if(split[0].equalsIgnoreCase("explode")) {
																	f69 = 4.0F;
																	if(split.length > 1) {
																		try {
																			f69 = (float)Integer.parseInt(split[1]);
																		} catch (Exception exception15) {
																			f69 = 4.0F;
																		}
																	}

																	this.mc.theWorld.func_12243_a(this.ep, this.ep.posX, this.ep.posY, this.ep.posZ, f69);
																} else if(split[0].equalsIgnoreCase("timeschedule")) {
																	if(split.length < 2) {
																		this.sendError(ERRMSG_PARAM);
																		return;
																	}

																	if(split[1].equalsIgnoreCase("reset")) {
																		this.timeschedule = null;
																		this.lastrift = -1;
																		this.sendMessage("Timeschedule reset");
																		this.saveSettings();
																		return;
																	}

																	if(split.length < 3) {
																		this.sendError(ERRMSG_PARAM);
																		return;
																	}

																	this.timeschedule = new int[4];

																	try {
																		this.timeschedule[2] = Integer.parseInt(split[1].split(":")[0]);
																		this.timeschedule[3] = Integer.parseInt(split[1].split(":")[1]);
																		this.timeschedule[0] = Integer.parseInt(split[2].split(":")[0]);
																		this.timeschedule[1] = Integer.parseInt(split[2].split(":")[1]);
																		this.timeschedule[2] = this.timeschedule[2] < 0 ? 0 : (this.timeschedule[2] > 23 ? 23 : this.timeschedule[2]);
																		this.timeschedule[3] = this.timeschedule[3] < 0 ? 0 : (this.timeschedule[3] > 59 ? 59 : this.timeschedule[3]);
																		this.timeschedule[0] = this.timeschedule[0] < 0 ? 0 : (this.timeschedule[0] > 23 ? 23 : this.timeschedule[0]);
																		this.timeschedule[1] = this.timeschedule[1] < 0 ? 0 : (this.timeschedule[1] > 59 ? 59 : this.timeschedule[1]);
																		this.lastrift = -1;
																	} catch (Exception exception14) {
																		this.sendError(ERRMSG_PARSE);
																		return;
																	}

																	this.sendMessage("Timeschedule set. From: " + split[1] + " To: " + split[2]);
																	this.saveSettings();
																} else if(split[0].equalsIgnoreCase("search")) {
																	if(split.length < 2) {
																		this.sendError(ERRMSG_PARAM);
																		return;
																	}

																	string36 = "";

																	for(npcs = 0; npcs < itemnames.size(); ++npcs) {
																		if((string37 = (String)itemnames.elementAt(npcs)) != null) {
																			string36 = string37.indexOf(split[1].trim().toLowerCase()) != -1 ? string36 + " " + string37 + "(" + npcs + ")" : string36;
																		}
																	}

																	if(string36.equalsIgnoreCase("")) {
																		this.sendMessage("No results found");
																	} else {
																		this.sendMessage(string36);
																	}
																} else if(split[0].equalsIgnoreCase("msg")) {
																	if(split.length < 2) {
																		this.sendError(ERRMSG_PARAM);
																		return;
																	}

																	string36 = "";

																	for(npcs = 1; npcs < split.length; ++npcs) {
																		string36 = string36 + split[npcs] + " ";
																	}

																	this.sendMessage(string36.trim());
																} else if(split[0].equalsIgnoreCase("grow")) {
																	s65 = 16;
																	if(split.length > 1 && split[1].equalsIgnoreCase("all")) {
																		s65 = 128;
																	}

																	npcs = MathHelper.floor_double(this.ep.posX);
																	i35 = MathHelper.floor_double(this.ep.posY);
																	i38 = MathHelper.floor_double(this.ep.posZ);
																	WorldGenTrees worldGenTrees62 = new WorldGenTrees();
																	WorldGenBigTree worldGenBigTree51 = new WorldGenBigTree();
																	Random r = new Random();

																	for(int i1 = 0; i1 < s65; ++i1) {
																		for(int j = 0; j < s65; ++j) {
																			if(i35 - j >= 0 && i35 + j <= 128) {
																				for(int k = 0; k < s65; ++k) {
																					Object object54;
																					if(r.nextInt(10) == 0) {
																						object54 = worldGenBigTree51;
																					} else {
																						object54 = worldGenTrees62;
																					}

																					this.growPlant(npcs + i1, i35 + j, i38 + k, r, (WorldGenerator)object54);
																					this.growPlant(npcs - i1, i35 + j, i38 + k, r, (WorldGenerator)object54);
																					this.growPlant(npcs - i1, i35 + j, i38 - k, r, (WorldGenerator)object54);
																					this.growPlant(npcs + i1, i35 + j, i38 - k, r, (WorldGenerator)object54);
																					this.growPlant(npcs + i1, i35 - j, i38 + k, r, (WorldGenerator)object54);
																					this.growPlant(npcs - i1, i35 - j, i38 + k, r, (WorldGenerator)object54);
																					this.growPlant(npcs - i1, i35 - j, i38 - k, r, (WorldGenerator)object54);
																					this.growPlant(npcs + i1, i35 - j, i38 - k, r, (WorldGenerator)object54);
																				}
																			}
																		}
																	}

																	this.sendMessage("Saplings and crops now matured.");
																} else if(split[0].equalsIgnoreCase("itemname")) {
																	if(this.ep.inventory.mainInventory[this.ep.inventory.currentItem] == null) {
																		this.sendMessage("No item currently selected.");
																		return;
																	}

																	i34 = this.ep.inventory.mainInventory[this.ep.inventory.currentItem].itemID;
																	String string66 = (String)itemnames.elementAt(i34);
																	string66 = string66 == null ? "Unknown" : string66;
																	this.sendMessage(string66 + " (" + i34 + ")");
																} else if(split[0].equalsIgnoreCase("useportal")) {
																	this.mc.func_6237_k();
																} else if(split[0].equalsIgnoreCase("replenish")) {
																	if(split.length > 1 && split[1].equalsIgnoreCase("all")) {
																		for(i34 = 0; i34 < this.ep.inventory.mainInventory.length; ++i34) {
																			if(this.ep.inventory.mainInventory[i34] != null) {
																				this.ep.inventory.mainInventory[i34].stackSize = this.ep.inventory.mainInventory[i34].getMaxStackSize();
																			}
																		}

																		for(i34 = 0; i34 < this.ep.inventory.armorInventory.length; ++i34) {
																			if(this.ep.inventory.armorInventory[i34] != null) {
																				this.ep.inventory.armorInventory[i34].stackSize = this.ep.inventory.armorInventory[i34].getMaxStackSize();
																			}
																		}

																		return;
																	}

																	if(this.ep.inventory.mainInventory[this.ep.inventory.currentItem] != null) {
																		this.ep.inventory.mainInventory[this.ep.inventory.currentItem].stackSize = this.ep.inventory.mainInventory[this.ep.inventory.currentItem].getMaxStackSize();
																	}
																} else if(split[0].equalsIgnoreCase("dropstore")) {
																	this.mc.theWorld.setBlock((int)this.ep.posX + 1, (int)this.ep.posY - 1, (int)this.ep.posZ, Block.crate.blockID);
																	this.mc.theWorld.setBlockWithNotify((int)this.ep.posX + 1, (int)this.ep.posY - 1, (int)this.ep.posZ + 1, Block.crate.blockID);
																	InventoryLargeChest inventoryLargeChest70 = new InventoryLargeChest("Large chest", (TileEntityChest)this.mc.theWorld.getBlockTileEntity((int)this.ep.posX + 1, (int)this.ep.posY - 1, (int)this.ep.posZ + 1), (TileEntityChest)this.mc.theWorld.getBlockTileEntity((int)this.ep.posX + 1, (int)this.ep.posY - 1, (int)this.ep.posZ));
																	npcs = 0;

																	for(i35 = 0; i35 < this.ep.inventory.mainInventory.length; ++i35) {
																		inventoryLargeChest70.setInventorySlotContents(npcs++, this.ep.inventory.mainInventory[i35]);
																		this.ep.inventory.mainInventory[i35] = null;
																	}

																	for(i35 = 0; i35 < this.ep.inventory.armorInventory.length; ++i35) {
																		inventoryLargeChest70.setInventorySlotContents(npcs++, this.ep.inventory.armorInventory[i35]);
																		this.ep.inventory.armorInventory[i35] = null;
																	}
																} else if(split[0].equalsIgnoreCase("removedrops")) {
																	s65 = 16;
																	if(split.length > 1 && split[1].equalsIgnoreCase("all")) {
																		s65 = 128;
																	}

																	list55 = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(this.ep, AxisAlignedBB.getBoundingBox(this.ep.posX - (double)s65, this.ep.posY - (double)s65, this.ep.posZ - (double)s65, this.ep.posX + (double)s65, this.ep.posY + (double)s65, this.ep.posZ + (double)s65));

																	for(i35 = 0; i35 < list55.size(); ++i35) {
																		entity49 = (Entity)list55.get(i35);
																		if(entity49 instanceof EntityItem) {
																			((EntityItem)entity49).setEntityDead();
																		}
																	}
																} else if(split[0].equalsIgnoreCase("help") || split[0].equalsIgnoreCase("h")) {
																	if(split.length > 1) {
																		if(split[1].startsWith("clear")) {
																			this.helpMessage("Clears the chat console.", "clear", "/clear");
																		} else if(!split[1].startsWith("give") && !split[1].startsWith("item")) {
																			if(split[1].startsWith("damage")) {
																				this.helpMessage("Turns damage on/off", "damage", "/damage");
																			} else if(split[1].startsWith("difficulty")) {
																				this.helpMessage("Sets the difficulty of the game. Valid values 0-3", "difficulty <VALUE>", "/difficulty 3");
																			} else if(split[1].startsWith("dropstore")) {
																				this.helpMessage("Transfers everything in your inventory into a chest that it creates next to you.", "dropstore", "/dropstore");
																			} else if(split[1].startsWith("extinguish")) {
																				this.helpMessage("Extinguishes fires nearby", "extinguish [all]", "/extinguish all");
																			} else if(split[1].startsWith("falldamage")) {
																				this.helpMessage("Turns fall damage on/off", "falldamage", "/falldamage");
																			} else if(split[1].startsWith("firedamage")) {
																				this.helpMessage("Turns fire damage on/off", "firedamage", "/firedamage");
																			} else if(split[1].startsWith("goto")) {
																				this.helpMessage("Goto a waypoint", "goto <NAME>", "/goto example");
																			} else if(split[1].startsWith("grow")) {
																				this.helpMessage("Grows all saplings/wheat on the map.", "grow [all]", "/grow");
																			} else if(split[1].startsWith("heal")) {
																				this.helpMessage("Heals a player the specified number of points", "heal <HEALTH>", "/heal 10");
																			} else if(split[1].startsWith("health")) {
																				this.helpMessage("Sets the health of a player to pre-defined figures", "health <MIN|MAX|INFINITE>", "/health max");
																			} else if(split[1].startsWith("help")) {
																				this.helpMessage("Brings up a help message", "help [COMMAND]", "/help give");
																			} else if(split[1].startsWith("home")) {
																				this.helpMessage("Teleport to spawn point", "home", "/home");
																			} else if(split[1].startsWith("instantmine")) {
																				this.helpMessage("Turns instant mining on/off", "instantmine", "/instantmine");
																			} else if(split[1].startsWith("itemname")) {
																				this.helpMessage("Discover the itemname and ID of your currently selected item.", "itemname", "/itemname");
																			} else if(split[1].startsWith("jump")) {
																				this.helpMessage("Moves you from where you are to where your mouse is pointing", "jump", "/jump");
																			} else if(split[1].startsWith("kill")) {
																				this.helpMessage("Kills the current player", "kill", "/kill");
																			} else if(split[1].startsWith("killnpc")) {
																				this.helpMessage("Kills all living creatures around the player.", "killnpc [all]", "/killnpc all");
																			} else if(split[1].startsWith("listwaypoints")) {
																				this.helpMessage("Lists all the waypoints currently configured.", "listwaypoints", "/listwaypoints");
																			} else if(split[1].startsWith("msg")) {
																				this.helpMessage("This commands adds a message to the console.", "msg <MESSAGE>", "/msg Hello world");
																			} else if(split[1].startsWith("music")) {
																				this.helpMessage("Music configuration. Send a request to start music or set the volume.", "music [volume]", "/music 50");
																			} else if(split[1].startsWith("pos")) {
																				this.helpMessage("Gives current player position", "pos", "/pos");
																			} else if(split[1].startsWith("rem")) {
																				this.helpMessage("Removes the specified waypoint", "rem <NAME>", "/rem example");
																			} else if(split[1].startsWith("removedrops")) {
																				this.helpMessage("This command removes item drops from the world.", "removedrops [all]", "/removedrops all");
																			} else if(split[1].startsWith("replenish")) {
																				this.helpMessage("Re-stocks your items in your inventory to the maximum ammount", "replenish [all]", "/replenish all");
																			} else if(split[1].startsWith("return")) {
																				this.helpMessage("Moves the player to the last position before teleport", "return", "return");
																			} else if(split[1].startsWith("search")) {
																				this.helpMessage("Allows you to search for items using a name", "search <SEARCHTERM>", "/search pick");
																			} else if(split[1].startsWith("setjump")) {
																				this.helpMessage("Sets the height that you jump", "setjump <HEIGHT|reset>", "/setjump 3");
																			} else if(split[1].startsWith("set")) {
																				this.helpMessage("Mark a waypoint on the world", "set <NAME>", "/set example");
																			} else if(split[1].startsWith("setspawn")) {
																				this.helpMessage("Set the current position as the spawn point, if X Y Z are specified sets that position as spawn point", "setspawn [<X> <Y> <Z>]", "/setspawn 0 66 0");
																			} else if(split[1].startsWith("setspeed")) {
																				this.helpMessage("Sets the speed that the player moves", "setspeed <SPEED|reset>", "/setspeed 3");
																			} else if(split[1].startsWith("spawn")) {
																				this.helpMessage("Spawns the specified creature.", "spawn <CREATURENAME> [QTY]", "/spawn zombie 10");
																			} else if(split[1].startsWith("spawnstack")) {
																				this.helpMessage("Spawns the specified creature, multiple creature will result in a stack.", "spawn <CREATURENAME>", "/spawn zombie");
																			} else if(split[1].startsWith("tele")) {
																				this.helpMessage("Teleport to X Y Z coordinates.", "tele <X> <Y> <Z>", "/tele 0 66 0");
																			} else if(split[1].startsWith("time")) {
																				this.helpMessage("Set and get the time within minecraft.", "time [set|get|day|night [minute|hour|day [TIME]]]", "/time set hour 16");
																			} else if(split[1].startsWith("timeschedule")) {
																				this.helpMessage("Sets a time schedule which minecraft time will follow", "timeschedule <TIME1> <TIME2>", "/timeschedule 0:00 12:00");
																			} else if(split[1].startsWith("useportal")) {
																				this.helpMessage("Instantly transfers you to the nether, use it again to go back.", "useportal", "/useportal");
																			} else if(split[1].startsWith("waterdamage")) {
																				this.helpMessage("Turns water damage on/off", "waterdamage", "/waterdamage");
																			}
																		} else {
																			this.helpMessage("Gives player item, if quantity isn\u2019t specified maximum amount of that item", "item <ITEMCODE|ITEMNAME> [QUANTITY]", "/item 1");
																		}
																	} else {
																		this.sendMessage("Commands:");
																		this.sendMessage("clear, damage, difficulty, dropstore, explode, extinguish, falldamage, firedamage, goto, grow, heal, health, help, home, instantmine, item, itemname, jump, kill, killnpc, listwaypoints, msg, music, pos, rem, removedrops, set, search, setjump, setspawn, setspeed, spawn, spawnstack, tele, time, timeschedule, useportal, waterdamage");
																		this.sendMessage("\"help COMMAND\" for more information about the command.");
																	}
																}
															} else {
																s65 = 16;
																if(split.length > 1 && split[1].equalsIgnoreCase("all")) {
																	s65 = 128;
																}

																npcs = MathHelper.floor_double(this.ep.posX);
																i35 = MathHelper.floor_double(this.ep.posY);
																i38 = MathHelper.floor_double(this.ep.posZ);

																for(sml = 0; sml < s65; ++sml) {
																	for(int i48 = 0; i48 < s65; ++i48) {
																		if(i35 - i48 >= 0 && i35 + i48 <= 128) {
																			for(int i50 = 0; i50 < s65; ++i50) {
																				if(this.mc.theWorld.getBlockId(npcs + sml, i35 + i48, i38 + i50) == Block.fire.blockID) {
																					this.mc.theWorld.setBlockWithNotify(npcs + sml, i35 + i48, i38 + i50, 0);
																				}

																				if(this.mc.theWorld.getBlockId(npcs - sml, i35 + i48, i38 + i50) == Block.fire.blockID) {
																					this.mc.theWorld.setBlockWithNotify(npcs - sml, i35 + i48, i38 + i50, 0);
																				}

																				if(this.mc.theWorld.getBlockId(npcs - sml, i35 + i48, i38 - i50) == Block.fire.blockID) {
																					this.mc.theWorld.setBlockWithNotify(npcs - sml, i35 + i48, i38 - i50, 0);
																				}

																				if(this.mc.theWorld.getBlockId(npcs + sml, i35 + i48, i38 - i50) == Block.fire.blockID) {
																					this.mc.theWorld.setBlockWithNotify(npcs + sml, i35 + i48, i38 - i50, 0);
																				}

																				if(this.mc.theWorld.getBlockId(npcs + sml, i35 - i48, i38 + i50) == Block.fire.blockID) {
																					this.mc.theWorld.setBlockWithNotify(npcs + sml, i35 - i48, i38 + i50, 0);
																				}

																				if(this.mc.theWorld.getBlockId(npcs - sml, i35 - i48, i38 + i50) == Block.fire.blockID) {
																					this.mc.theWorld.setBlockWithNotify(npcs - sml, i35 - i48, i38 + i50, 0);
																				}

																				if(this.mc.theWorld.getBlockId(npcs - sml, i35 - i48, i38 - i50) == Block.fire.blockID) {
																					this.mc.theWorld.setBlockWithNotify(npcs - sml, i35 - i48, i38 - i50, 0);
																				}

																				if(this.mc.theWorld.getBlockId(npcs + sml, i35 - i48, i38 - i50) == Block.fire.blockID) {
																					this.mc.theWorld.setBlockWithNotify(npcs + sml, i35 - i48, i38 - i50, 0);
																				}
																			}
																		}
																	}
																}

																this.ep.fire = 0;
																this.sendMessage("Fire extinguished");
															}
														}
													}
												} else {
													if(split.length > 1 && split[1].equalsIgnoreCase("all")) {
														for(i34 = 0; i34 < this.ep.inventory.mainInventory.length; ++i34) {
															if(this.ep.inventory.mainInventory[i34] != null) {
																this.ep.dropPlayerItem(this.ep.inventory.mainInventory[i34].copy());
															}
														}

														for(i34 = 0; i34 < this.ep.inventory.armorInventory.length; ++i34) {
															if(this.ep.inventory.armorInventory[i34] != null) {
																this.ep.dropPlayerItem(this.ep.inventory.armorInventory[i34].copy());
															}
														}

														return;
													}

													if(this.ep.inventory.getCurrentItem() != null) {
														if(split.length > 1) {
															try {
																i34 = Integer.parseInt(split[1]);
																i34 = i34 > 256 ? 256 : (i34 < 0 ? 0 : i34);

																for(npcs = 0; npcs < i34; ++npcs) {
																	this.ep.dropPlayerItem(this.ep.inventory.getCurrentItem().copy());
																}
															} catch (Exception exception28) {
																this.sendError(ERRMSG_PARSE);
															}

															return;
														}

														this.ep.dropPlayerItem(this.ep.inventory.getCurrentItem().copy());
													}
												}
											} else {
												boolean z67 = false;
												boolean z57 = true;
												if(split[0].equalsIgnoreCase("descend")) {
													z57 = false;
												}

												i = this.ep.posX;
												d42 = this.ep.posY;
												double wgt = this.ep.posZ;
												this.setCurrentPosition();

												label1205: {
													while(this.ep.posY > 0.0D && this.ep.posY < 128.0D) {
														this.ep.setPosition(this.ep.posX, this.ep.posY, this.ep.posZ);
														if(this.mc.theWorld.getCollidingBoundingBoxes(this.ep, this.ep.boundingBox).size() == 0) {
															if(z57 && z67) {
																break label1205;
															}

															if(!z57 && z67) {
																this.ep.setPosition(this.ep.posX, --this.ep.posY, this.ep.posZ);
																if(this.mc.theWorld.getCollidingBoundingBoxes(this.ep, this.ep.boundingBox).size() != 0) {
																	this.ep.setPosition(this.ep.posX, ++this.ep.posY, this.ep.posZ);
																	break label1205;
																}

																++this.ep.posY;
															}
														} else {
															z67 = true;
														}

														if(z57) {
															++this.ep.posY;
														} else {
															--this.ep.posY;
														}
													}

													this.ep.setPosition(i, d42, wgt);
												}

												this.ep.motionX = this.ep.motionY = this.ep.motionZ = 0.0D;
												this.ep.rotationPitch = 0.0F;
											}
										} else {
											if(split.length < 2) {
												this.sendError(ERRMSG_PARAM);
												return;
											}

											try {
												i34 = Integer.parseInt(split[1]);
												if(i34 < 0) {
													i34 = 0;
												} else if(i34 > 3) {
													i34 = 3;
												}

												this.mc.gameSettings.difficulty = i34;
											} catch (Exception exception19) {
												this.sendError(ERRMSG_PARSE);
												return;
											}
										}
									}
								}
							} else {
								if(split.length < 2) {
									this.sendError(ERRMSG_PARAM);
									return;
								}

								string36 = s.substring(4).trim();
								waypoints.put(string36, new double[]{this.ep.posX, this.ep.posY, this.ep.posZ});
								this.writeWaypointsToNBT(this.mc.theWorld.field_9432_t);
								this.sendMessage("Waypoint \"" + string36 + "\" set at: " + this.positionAsString());
							}
						}
					} else {
						this.sendMessage("Current Position: " + this.positionAsString());
					}
				} else {
					if(split.length < 4) {
						this.sendError(ERRMSG_PARAM);
						return;
					}

					try {
						d33 = Double.parseDouble(split[1]);
						i = Double.parseDouble(split[2]);
						d42 = Double.parseDouble(split[3]);
						this.setCurrentPosition();
						this.ep.setPosition(d33, i, d42);
					} catch (Exception exception27) {
						this.sendError(ERRMSG_PARSE);
					}
				}
			} else {
				if(split.length < 2) {
					this.sendError(ERRMSG_PARAM);
					return;
				}

				distance = null;
				npcs = 1;

				try {
					distance = Item.itemsList[Integer.parseInt(split[1])];
					npcs = distance.getItemStackLimit();
					if(split.length > 2) {
						npcs = Integer.parseInt(split[2]);
					}
				} catch (Exception exception29) {
					e = "";

					for(sml = 1; sml < split.length; ++sml) {
						e = e + split[sml] + " ";
					}

					sml = itemnames.indexOf(e.trim().toLowerCase());
					if(sml > -1) {
						distance = Item.itemsList[sml];
						npcs = distance.getItemStackLimit();
					}
				}

				if(distance != null) {
					this.ep.dropPlayerItem(new ItemStack(distance, npcs));
				} else {
					this.sendError("Could not find specified item.");
				}
			}

		}
	}

	public void processWorldEdit(String command) throws Exception {
		if(command != null) {
			String[] split = command.trim().split(" ");
			split[0] = (String)this.commands.get(split[0]);
			if(split[0] == null) {
				this.sendError("WorldEdit command does not exist.");
			} else {
				if(split[0].startsWith("/")) {
					split[0] = split[0].substring(1);
				}

				if(split[0].toLowerCase().startsWith("set1")) {
					this.cubeposx[0] = MathHelper.floor_double(this.ep.posX);
					this.cubeposy[0] = MathHelper.floor_double(this.ep.posY - 1.0D);
					this.cubeposz[0] = MathHelper.floor_double(this.ep.posZ);
					this.sendMessage("[Cuboid] Position 1 set " + this.positionAsString());
				} else if(split[0].toLowerCase().startsWith("set2")) {
					this.cubeposx[1] = MathHelper.floor_double(this.ep.posX);
					this.cubeposy[1] = MathHelper.floor_double(this.ep.posY - 1.0D);
					this.cubeposz[1] = MathHelper.floor_double(this.ep.posZ);
					this.sendMessage("[Cuboid] Position 2 set " + this.positionAsString());
				} else {
					int i22;
					if(split[0].equalsIgnoreCase("reset")) {
						for(i22 = 0; i22 < this.cubeposx.length; ++i22) {
							this.cubeposx[i22] = 0;
							this.cubeposy[i22] = 0;
							this.cubeposz[i22] = 0;
						}

						this.sendMessage("[Cuboid] Positions reset");
					} else {
						int e;
						int e1;
						int pos;
						int lines;
						int ilim;
						int jlim;
						if(!split[0].equalsIgnoreCase("fill") && !split[0].equalsIgnoreCase("remove")) {
							if(split[0].equalsIgnoreCase("box")) {
								try {
									i22 = Integer.parseInt(split[1]);
									e = this.cubeposx[0] < this.cubeposx[1] ? this.cubeposx[1] - this.cubeposx[0] : this.cubeposx[0] - this.cubeposx[1];
									e1 = this.cubeposy[0] < this.cubeposy[1] ? this.cubeposy[1] - this.cubeposy[0] : this.cubeposy[0] - this.cubeposy[1];
									pos = this.cubeposz[0] < this.cubeposz[1] ? this.cubeposz[1] - this.cubeposz[0] : this.cubeposz[0] - this.cubeposz[1];

									for(lines = 0; lines <= e; ++lines) {
										for(ilim = 0; ilim <= e1; ++ilim) {
											for(jlim = 0; jlim <= pos; ++jlim) {
												if(lines != 0 && lines != e && ilim != 0 && ilim != e1 && jlim != 0 && jlim != pos) {
													this.mc.theWorld.setBlockWithNotify(this.cubeposx[0] < this.cubeposx[1] ? this.cubeposx[1] - lines : this.cubeposx[1] + lines, this.cubeposy[0] < this.cubeposy[1] ? this.cubeposy[1] - ilim : this.cubeposy[1] + ilim, this.cubeposz[0] < this.cubeposz[1] ? this.cubeposz[1] - jlim : this.cubeposz[1] + jlim, 0);
												} else {
													this.mc.theWorld.setBlockWithNotify(this.cubeposx[0] < this.cubeposx[1] ? this.cubeposx[1] - lines : this.cubeposx[1] + lines, this.cubeposy[0] < this.cubeposy[1] ? this.cubeposy[1] - ilim : this.cubeposy[1] + ilim, this.cubeposz[0] < this.cubeposz[1] ? this.cubeposz[1] - jlim : this.cubeposz[1] + jlim, i22);
												}
											}
										}
									}

									this.sendMessage("[Cuboid] Box created");
								} catch (Exception exception21) {
									this.sendError(ERRMSG_PARSE);
								}
							} else if(split[0].equalsIgnoreCase("walls")) {
								try {
									i22 = Integer.parseInt(split[1]);
									e = this.cubeposx[0] < this.cubeposx[1] ? this.cubeposx[1] - this.cubeposx[0] : this.cubeposx[0] - this.cubeposx[1];
									e1 = this.cubeposy[0] < this.cubeposy[1] ? this.cubeposy[1] - this.cubeposy[0] : this.cubeposy[0] - this.cubeposy[1];
									pos = this.cubeposz[0] < this.cubeposz[1] ? this.cubeposz[1] - this.cubeposz[0] : this.cubeposz[0] - this.cubeposz[1];

									for(lines = 0; lines <= e; ++lines) {
										for(ilim = 0; ilim <= e1; ++ilim) {
											for(jlim = 0; jlim <= pos; ++jlim) {
												if(lines != 0 && lines != e && jlim != 0 && jlim != pos) {
													this.mc.theWorld.setBlockWithNotify(this.cubeposx[0] < this.cubeposx[1] ? this.cubeposx[1] - lines : this.cubeposx[1] + lines, this.cubeposy[0] < this.cubeposy[1] ? this.cubeposy[1] - ilim : this.cubeposy[1] + ilim, this.cubeposz[0] < this.cubeposz[1] ? this.cubeposz[1] - jlim : this.cubeposz[1] + jlim, 0);
												} else {
													this.mc.theWorld.setBlockWithNotify(this.cubeposx[0] < this.cubeposx[1] ? this.cubeposx[1] - lines : this.cubeposx[1] + lines, this.cubeposy[0] < this.cubeposy[1] ? this.cubeposy[1] - ilim : this.cubeposy[1] + ilim, this.cubeposz[0] < this.cubeposz[1] ? this.cubeposz[1] - jlim : this.cubeposz[1] + jlim, i22);
												}
											}
										}
									}

									this.sendMessage("[Cuboid] Walls created");
								} catch (Exception exception20) {
									this.sendError(ERRMSG_PARSE);
								}
							} else {
								if(split[0].equalsIgnoreCase("get")) {
									this.sendMessage("[Cuboid] Position 1: (" + this.cubeposx[0] + "," + this.cubeposy[0] + "," + this.cubeposz[0] + ") Position 2: (" + this.cubeposx[1] + "," + this.cubeposy[1] + "," + this.cubeposz[1] + ")");
									return;
								}

								if(split[0].equalsIgnoreCase("copy")) {
									this.clipboard = this.cuboidCopy(this.cubeposx[0], this.cubeposy[0], this.cubeposz[0], this.cubeposx[1], this.cubeposy[1], this.cubeposz[1]);
									this.sendMessage("[Cuboid] Area copied");
								} else if(split[0].equalsIgnoreCase("paste")) {
									if(this.clipboard == null) {
										this.sendError("You need to copy something before pasting.");
										return;
									}

									this.cuboidPaste(this.clipboard, (int)this.ep.posX, (int)this.ep.posY, (int)this.ep.posZ);
									this.sendMessage("[Cuboid] Area pasted");
								} else {
									int klim;
									int i;
									Minecraft minecraft10002;
									FileInputStream fileInputStream24;
									if(split[0].equalsIgnoreCase("save")) {
										if(this.clipboard == null) {
											this.sendError("You need to copy something before pasting");
											return;
										}

										String string23 = null;

										try {
											fileInputStream24 = null;
											minecraft10002 = this.mc;
											File file25 = new File(Minecraft.getMinecraftDir(), "mods/sppcommands/saves");
											if(split.length > 1) {
												string23 = split[1];
											} else {
												string23 = "save";
											}

											if(!file25.exists()) {
												file25.mkdirs();
											}

											pos = 0;
											if((new File(file25, string23)).exists()) {
												while((new File(file25, string23 + pos++)).exists()) {
												}

												StringBuilder stringBuilder10000 = (new StringBuilder()).append(string23);
												--pos;
												string23 = stringBuilder10000.append(pos).toString();
											}

											FileOutputStream fileOutputStream30 = new FileOutputStream(new File(file25, string23));
											ilim = this.clipboard.length;
											jlim = this.clipboard[0].length;
											klim = this.clipboard[0][0].length;
											fileOutputStream30.write((ilim + "," + jlim + "," + klim + "\n").getBytes());

											for(i = 0; i < ilim; ++i) {
												for(int w = 0; w < jlim; ++w) {
													for(int xyz = 0; xyz < klim; ++xyz) {
														fileOutputStream30.write(("(" + i + "," + w + "," + xyz + "," + this.clipboard[i][w][xyz] + ")\n").getBytes());
													}
												}
											}

											fileOutputStream30.close();
											this.sendMessage("[Cuboid] Area saved to: " + string23);
										} catch (Exception exception19) {
											this.sendError("Could not write to file.");
											return;
										}
									} else if(split[0].equalsIgnoreCase("load")) {
										if(split.length < 2) {
											this.sendError(ERRMSG_PARAM);
											return;
										}

										minecraft10002 = this.mc;
										File file27 = new File(Minecraft.getMinecraftDir(), "mods/sppcommands/saves");
										if(!file27.exists()) {
											this.sendError("Specified file does not exist.");
											return;
										}

										if(!(new File(file27, split[1])).exists()) {
											this.sendError("Specified file does not exist.");
											return;
										}

										try {
											fileInputStream24 = new FileInputStream(new File(file27, split[1]));
											StringBuffer stringBuffer26 = new StringBuffer("");
											boolean z29 = true;

											label466:
											while(true) {
												if((pos = fileInputStream24.read()) == -1) {
													fileInputStream24.close();
													String[] string31 = stringBuffer26.toString().split("\n");
													ilim = Integer.parseInt(string31[0].split(",")[0]);
													jlim = Integer.parseInt(string31[0].split(",")[1]);
													klim = Integer.parseInt(string31[0].split(",")[2]);
													this.clipboard = new int[ilim][jlim][klim];
													i = 1;

													while(true) {
														if(i >= string31.length) {
															break label466;
														}

														String[] string32 = string31[i].substring(1, string31[i].length() - 1).split(",");
														int[] i33 = new int[string32.length];

														for(int j = 0; j < i33.length; ++j) {
															i33[j] = Integer.parseInt(string32[j]);
														}

														this.clipboard[i33[0]][i33[1]][i33[2]] = i33[3];
														++i;
													}
												}

												stringBuffer26.append((char)pos);
											}
										} catch (Exception exception18) {
											this.sendError("Could not read the file.");
											return;
										}

										this.sendMessage("[Cuboid] File loaded");
									} else {
										double d28;
										if(split[0].equalsIgnoreCase("expand")) {
											if(split.length < 2) {
												this.sendError(ERRMSG_PARAM);
												return;
											}

											d28 = 1.0D;
											if(split.length > 2) {
												try {
													d28 = Double.parseDouble(split[2]);
												} catch (Exception exception16) {
													d28 = 1.0D;
												}
											}

											if(split[1].toLowerCase().startsWith("s")) {
												if(this.cubeposx[0] > this.cubeposx[1]) {
													this.cubeposx[0] = (int)((double)this.cubeposx[0] + d28);
												} else {
													this.cubeposx[1] = (int)((double)this.cubeposx[1] + d28);
												}
											} else if(split[1].toLowerCase().startsWith("n")) {
												if(this.cubeposx[0] < this.cubeposx[1]) {
													this.cubeposx[0] = (int)((double)this.cubeposx[0] - d28);
												} else {
													this.cubeposx[1] = (int)((double)this.cubeposx[1] - d28);
												}
											} else if(split[1].toLowerCase().startsWith("w")) {
												if(this.cubeposz[0] > this.cubeposz[1]) {
													this.cubeposz[0] = (int)((double)this.cubeposz[0] + d28);
												} else {
													this.cubeposz[1] = (int)((double)this.cubeposz[1] + d28);
												}
											} else if(split[1].toLowerCase().startsWith("e")) {
												if(this.cubeposz[0] < this.cubeposz[1]) {
													this.cubeposz[0] = (int)((double)this.cubeposz[0] - d28);
												} else {
													this.cubeposz[1] = (int)((double)this.cubeposz[1] - d28);
												}
											} else if(split[1].toLowerCase().startsWith("u")) {
												if(this.cubeposy[0] > this.cubeposy[1]) {
													this.cubeposy[0] = (int)((double)this.cubeposy[0] + d28);
												} else {
													this.cubeposy[1] = (int)((double)this.cubeposy[1] + d28);
												}
											} else {
												if(!split[1].toLowerCase().startsWith("d")) {
													this.sendError("Unknown direction for expand.");
													return;
												}

												if(this.cubeposy[0] < this.cubeposy[1]) {
													this.cubeposy[0] = (int)((double)this.cubeposy[0] - d28);
												} else {
													this.cubeposy[1] = (int)((double)this.cubeposy[1] - d28);
												}
											}

											this.sendMessage("[Cuboid] Area expanded " + split[1]);
										} else if(split[0].equalsIgnoreCase("contract")) {
											if(split.length < 2) {
												this.sendError(ERRMSG_PARAM);
												return;
											}

											d28 = 1.0D;
											if(split.length > 2) {
												try {
													d28 = Double.parseDouble(split[2]);
												} catch (Exception exception15) {
													d28 = 1.0D;
												}
											}

											if(split[1].toLowerCase().startsWith("s")) {
												if(this.cubeposx[0] > this.cubeposx[1]) {
													this.cubeposx[0] = (int)((double)this.cubeposx[0] - d28);
												} else {
													this.cubeposx[1] = (int)((double)this.cubeposx[1] - d28);
												}
											} else if(split[1].toLowerCase().startsWith("n")) {
												if(this.cubeposx[0] < this.cubeposx[1]) {
													this.cubeposx[0] = (int)((double)this.cubeposx[0] + d28);
												} else {
													this.cubeposx[1] = (int)((double)this.cubeposx[1] + d28);
												}
											} else if(split[1].toLowerCase().startsWith("w")) {
												if(this.cubeposz[0] > this.cubeposz[1]) {
													this.cubeposz[0] = (int)((double)this.cubeposz[0] - d28);
												} else {
													this.cubeposz[1] = (int)((double)this.cubeposz[1] - d28);
												}
											} else if(split[1].toLowerCase().startsWith("e")) {
												if(this.cubeposz[0] < this.cubeposz[1]) {
													this.cubeposz[0] = (int)((double)this.cubeposz[0] + d28);
												} else {
													this.cubeposz[1] = (int)((double)this.cubeposz[1] + d28);
												}
											} else if(split[1].toLowerCase().startsWith("u")) {
												if(this.cubeposy[0] > this.cubeposy[1]) {
													this.cubeposy[0] = (int)((double)this.cubeposy[0] - d28);
												} else {
													this.cubeposy[1] = (int)((double)this.cubeposy[1] - d28);
												}
											} else {
												if(!split[1].toLowerCase().startsWith("d")) {
													this.sendError("Unknown direction for contract.");
													return;
												}

												if(this.cubeposy[0] < this.cubeposy[1]) {
													this.cubeposy[0] = (int)((double)this.cubeposy[0] + d28);
												} else {
													this.cubeposy[1] = (int)((double)this.cubeposy[1] + d28);
												}
											}

											this.sendMessage("[Cuboid] Area contracted " + split[1]);
										} else if(split[0].equalsIgnoreCase("help")) {
											if(split.length < 2) {
												this.sendMessage("World Edit Commands:");
												this.sendMessage("box, contract, copy, expand, fill, help, load, paste, remove, reset, save, set1, set2, walls");
												this.sendMessage("\"//help COMMAND\" for more information about the command.");
												return;
											}

											if(split[1].equalsIgnoreCase("box")) {
												this.helpMessage("Creates a box between the two specified points", "//box <BLOCKID>", "//box 1");
											} else if(split[1].equalsIgnoreCase("contract")) {
												this.helpMessage("Reduces the selected area in the direction specified", "//contract <n|s|e|w|u|d> [DISTANCE]", "//contract north 10");
											} else if(split[1].equalsIgnoreCase("copy")) {
												this.helpMessage("Copies the selected area", "//copy", "//copy");
											} else if(split[1].equalsIgnoreCase("expand")) {
												this.helpMessage("Expands the selected area in the direction specified", "//expand <n|s|e|w|u|d> [DISTANCE]", "//expand up 10");
											} else if(split[1].equalsIgnoreCase("fill")) {
												this.helpMessage("Fills the selected area", "//fill <BLOCKID>", "//fill 1");
											} else if(split[1].equalsIgnoreCase("help")) {
												this.helpMessage("Specific world-edit help", "//help [COMMAND]", "//help help");
											} else if(split[1].equalsIgnoreCase("load")) {
												this.helpMessage("Loads a saved config file so that it can be pasted", "//load <FILENAME>", "//load save");
											} else if(split[1].equalsIgnoreCase("paste")) {
												this.helpMessage("\"Pastes\" the copied (or loaded) region to the south-west of you", "//paste", "//paste");
											} else if(split[1].equalsIgnoreCase("remove")) {
												this.helpMessage("Removes the selected region", "//remove", "//remove");
											} else if(split[1].equalsIgnoreCase("reset")) {
												this.helpMessage("Resets the selected region", "//reset", "//reset");
											} else if(split[1].equalsIgnoreCase("save")) {
												this.helpMessage("Saves the selected region to file", "//save [FILENAME]", "//save example");
											} else if(split[1].equalsIgnoreCase("set1")) {
												this.helpMessage("Sets one corner of the region", "//set1", "//set1");
											} else if(split[1].equalsIgnoreCase("set2")) {
												this.helpMessage("Sets one corner of the region", "//set2", "//set2");
											} else if(split[1].equalsIgnoreCase("walls")) {
												this.helpMessage("Puts walls around the selected region", "//walls <BLOCKID>", "//walls 1");
											}
										}
									}
								}
							}
						} else {
							if(split.length < 2 && !split[0].equalsIgnoreCase("remove")) {
								this.sendError(ERRMSG_PARAM);
								return;
							}

							if(this.cubeposy[0] <= 0 || this.cubeposy[1] <= 0) {
								this.sendError(ERRMSG_NOTSET);
								return;
							}

							try {
								boolean qty = true;
								if(split[0].equalsIgnoreCase("fill")) {
									i22 = Integer.parseInt(split[1]);
								} else {
									i22 = 0;
								}

								e = this.cubeposx[0] < this.cubeposx[1] ? this.cubeposx[1] - this.cubeposx[0] : this.cubeposx[0] - this.cubeposx[1];
								e1 = this.cubeposy[0] < this.cubeposy[1] ? this.cubeposy[1] - this.cubeposy[0] : this.cubeposy[0] - this.cubeposy[1];
								pos = this.cubeposz[0] < this.cubeposz[1] ? this.cubeposz[1] - this.cubeposz[0] : this.cubeposz[0] - this.cubeposz[1];

								for(lines = 0; lines <= e; ++lines) {
									for(ilim = 0; ilim <= e1; ++ilim) {
										for(jlim = 0; jlim <= pos; ++jlim) {
											this.mc.theWorld.setBlockWithNotify(this.cubeposx[0] < this.cubeposx[1] ? this.cubeposx[1] - lines : this.cubeposx[1] + lines, this.cubeposy[0] < this.cubeposy[1] ? this.cubeposy[1] - ilim : this.cubeposy[1] + ilim, this.cubeposz[0] < this.cubeposz[1] ? this.cubeposz[1] - jlim : this.cubeposz[1] + jlim, i22);
										}
									}
								}

								if(split[0].equalsIgnoreCase("fill")) {
									this.sendMessage("[Cuboid] Area filled");
								} else {
									this.sendMessage("[Cuboid] Area removed");
								}
							} catch (Exception exception17) {
								this.sendError(ERRMSG_PARSE);
							}
						}
					}
				}

				this.saveSettings();
			}
		}
	}

	public void sendMessage(String message) {
	}

	public void sendMessage(String message, char colour) {
	}

	public void sendError(String message) {
	}

	public void printCurrentTime() {
		int[] temp = this.getTime();
		String hr = temp[1] < 10 ? "0" + temp[1] : "" + temp[1];
		String mn = temp[0] < 10 ? "0" + temp[0] : "" + temp[0];
		this.sendMessage("Day: " + temp[2] + " at " + hr + ":" + mn);
	}

	public int[] getTime() {
		int DD = (int)(this.mc.theWorld.worldTime / 1000L / 24L);
		int HH = (int)(this.mc.theWorld.worldTime / 1000L % 24L);
		int MM = (int)((double)(this.mc.theWorld.worldTime % 1000L) / 1000.0D * 60.0D);
		return new int[]{MM, HH, DD};
	}

	public void helpMessage(String s1, String s2, String s3) {
		this.sendMessage("Description:");
		this.sendMessage("\t" + s1);
		this.sendMessage("Syntax:");
		this.sendMessage("\t" + s2);
		this.sendMessage("Example:");
		this.sendMessage("\t" + s3);
	}

	public String positionAsString() {
		DecimalFormat f = new DecimalFormat("#.##");
		return "(" + f.format(this.ep.posX) + "," + f.format(this.ep.posY) + "," + f.format(this.ep.posZ + 1.0D) + ")";
	}

	public int[][][] cuboidCopy(int i, int j, int k, int x, int y, int z) {
		int[][][] blocks = (int[][][])null;
		int ilim = (i < x ? x - i : i - x) + 1;
		int jlim = (j < y ? y - j : j - y) + 1;
		int klim = (k < z ? z - k : k - z) + 1;
		blocks = new int[ilim][jlim][klim];
		int lwrx = i < x ? i : x;
		int lwry = j < y ? j : y;
		int lwrz = k < z ? k : z;

		for(int i1 = 0; i1 < ilim; ++i1) {
			for(int j1 = 0; j1 < jlim; ++j1) {
				for(int k1 = 0; k1 < klim; ++k1) {
					blocks[i1][j1][k1] = this.mc.theWorld.getBlockId(lwrx + i1, lwry + j1, lwrz + k1);
				}
			}
		}

		return blocks;
	}

	public void cuboidPaste(int[][][] blocks, int i, int j, int k) {
		if(blocks != null) {
			int ilim = blocks.length;
			int jlim = blocks[0].length;
			int klim = blocks[0][0].length;

			for(int i1 = 0; i1 < ilim; ++i1) {
				for(int j1 = 0; j1 < jlim; ++j1) {
					for(int k1 = 0; k1 < klim; ++k1) {
						this.mc.theWorld.setBlockWithNotify(i + i1, j + j1 - 1, k + k1, blocks[i1][j1][k1]);
					}
				}
			}

		}
	}

	public void beforeUpdate() {
	}

	public void afterUpdate() {
		if(!this.waterdamage) {
			this.ep.air = this.ep.field_9308_bh;
		}

		if(!this.damage) {
			this.ep.hurtTime = 0;
			this.ep.field_9306_bj = 0;
		}

		if(this.timeschedule != null) {
			int[] temp = this.getTime();
			if(this.lastrift != temp[2] && (temp[1] > this.timeschedule[2] || temp[1] >= this.timeschedule[2] && temp[0] > this.timeschedule[3])) {
				byte day = 0;
				if(this.timeschedule[0] < this.timeschedule[2] || this.timeschedule[0] <= this.timeschedule[2] && this.timeschedule[1] < this.timeschedule[2]) {
					day = 1;
				}

				this.mc.theWorld.worldTime = (long)((temp[2] + day) * 24000 + this.timeschedule[0] % 24 * 1000 + (int)((double)(this.timeschedule[1] % 60) / 60.0D * 1000.0D));
				this.lastrift = temp[2];
				this.saveSettings();
			}
		}

	}

	public void populateItemNames() {
		if(itemnames == null) {
			itemnames = new Vector();

			for(int i = 0; i < Item.itemsList.length; ++i) {
				if(Item.itemsList[i] == null) {
					itemnames.add((Object)null);
				}
			}

		}
	}

	public void growPlant(int i, int j, int k, Random r, WorldGenerator wgt) {
		if(this.mc.theWorld.getBlockId(i, j, k) == Block.sapling.blockID) {
			this.mc.theWorld.setBlock(i, j, k, 0);
			if(!wgt.generate(this.mc.theWorld, r, i, j, k)) {
				this.mc.theWorld.setBlock(i, j, k, Block.sapling.blockID);
				this.mc.theWorld.setBlockMetadataWithNotify(i, j, k, 15);
			}

		} else if(this.mc.theWorld.getBlockId(i, j, k) == Block.crops.blockID) {
			this.mc.theWorld.setBlockMetadataWithNotify(i, j, k, 7);
		}
	}
}
