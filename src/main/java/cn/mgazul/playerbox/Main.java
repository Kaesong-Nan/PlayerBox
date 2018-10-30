package cn.mgazul.playerbox;

import cn.mgazul.pfcorelib.FCoinsAPI;
import cn.mgazul.pfcorelib.MoneyAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main extends JavaPlugin implements Listener
{
    private String Prefix;
    private HashMap<Player, Inventory> PlayerGUI;
    private HashMap<Player, Integer> PlayerGUIPage;
    private HashMap<Player, Integer> PlayerGUIHang;
    private HashMap<Player, Boolean> PlayerGUIJieSuoOK;
    
    public Main() {
        this.Prefix = "§7[§c系统§7] ";
        this.PlayerGUI = new HashMap();
        this.PlayerGUIPage = new HashMap();
        this.PlayerGUIHang = new HashMap();
        this.PlayerGUIJieSuoOK = new HashMap();
    }
    
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage(String.valueOf(this.Prefix) + "正在加载玩家仓库插件...");
        Bukkit.getConsoleSender().sendMessage(String.valueOf(this.Prefix) + "Loading监听器...");
        this.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getConsoleSender().sendMessage(String.valueOf(this.Prefix) + "监听器加载完毕");
    }
    
    public static void configsave(final File File2) {
        String tempM = "";
        try {
            final InputStreamReader read = new InputStreamReader(new FileInputStream(File2));
            final BufferedReader reader = new BufferedReader(read);
            String line = "";
            while ((line = reader.readLine()) != null) {
                tempM = String.valueOf(tempM) + line + "\n";
            }
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tempM.length(); ++i) {
                final char char1 = tempM.charAt(i);
                if (char1 == '\\' && isUnicode(tempM, i)) {
                    final String cStr = tempM.substring(i + 2, i + 6);
                    final int cInt = Integer.parseInt(cStr, 16);
                    sb.append((char)cInt);
                    i += 5;
                }
                else {
                    sb.append(char1);
                }
            }
            final Writer writer = new OutputStreamWriter(new FileOutputStream(File2));
            writer.write(sb.toString());
            read.close();
            reader.close();
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getBukkitVersion() {
        String Ver = "";
        final String StrVer = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        for (int a = 0; a < StrVer.length(); ++a) {
            if (this.isNum(new StringBuilder().append(StrVer.charAt(a)).toString()) && Ver.length() < 3) {
                Ver = String.valueOf(Ver) + StrVer.charAt(a);
                if (Ver.length() == 1) {
                    Ver = String.valueOf(Ver) + ".";
                }
            }
        }
        return Ver;
    }
    
    public void saveconfig(final File file, final FileConfiguration F) {
        try {
            F.save(file);
        }
        catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§4文件保存出错!");
        }
    }
    
    public boolean isNum(final String str) {
        return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player)sender;
            if (label.equalsIgnoreCase("box")) {
                if (args.length == 0) {
                    this.PlayerOpenBox(p, 1);
                    return true;
                }
                if (args.length == 1) {
                	MoneyAPI.addMoneys(p.getUniqueId(), 100.0);
                        if (this.isNum(args[0])) {
                            final int Page = Integer.valueOf(args[0]);
                            if (Page > 0) {
                                this.PlayerOpenBox(p, Page);
                            }
                            else {
                                p.sendMessage(String.valueOf(this.Prefix) + "§4指令参数错误，不能为0！");
                            }
                            return true;
                        }
                        p.sendMessage(String.valueOf(this.Prefix) + "§4指令参数错误，打开指定页数仓库请使用/Box [页数]");                  
                }
            }
        }
        return false;
    }
    
    @EventHandler
    public void onInventoryCloseEvent(final InventoryCloseEvent evt) {
        final Inventory GUI = evt.getInventory();
        if (this.PlayerGUI.containsKey(evt.getPlayer()) && this.PlayerGUI.get(evt.getPlayer()).equals(GUI)) {
            this.SavePlayerBox((Player)evt.getPlayer(), GUI, this.PlayerGUIPage.get(evt.getPlayer()));
            if (this.PlayerGUI.containsKey(evt.getPlayer())) {
                this.PlayerGUI.remove(evt.getPlayer());
            }
        }
    }
    
    @EventHandler
    public void onInventoryClickEvent(final InventoryClickEvent evt) {
        if (evt.getWhoClicked() instanceof Player) {
            final Player p = (Player)evt.getWhoClicked();
            final Inventory GUI = evt.getInventory();
            if (this.PlayerGUI.containsKey(p) && this.PlayerGUI.get(p).equals(GUI)) {
                final FileConfiguration PlayerData = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + "/PlayerData/" + p.getName() + ".yml"));
                final int Click = evt.getRawSlot();
                if (Click >= 45 && Click <= 53) {
                    evt.setCancelled(true);
                }
                if (!PlayerData.getBoolean("仓库." + this.PlayerGUIPage.get(p) + ".是否解锁完成")) {
                    if (Click >= 0 && Click <= 8) {
                        if (!PlayerData.getBoolean("仓库." + this.PlayerGUIPage.get(p) + ".已解锁层数.1")) {
                            evt.setCancelled(true);
                        }
                    }
                    else if (Click >= 9 && Click <= 17) {
                        if (!PlayerData.getBoolean("仓库." + this.PlayerGUIPage.get(p) + ".已解锁层数.2")) {
                            evt.setCancelled(true);
                        }
                    }
                    else if (Click >= 18 && Click <= 26) {
                        if (!PlayerData.getBoolean("仓库." + this.PlayerGUIPage.get(p) + ".已解锁层数.3")) {
                            evt.setCancelled(true);
                        }
                    }
                    else if (Click >= 27 && Click <= 35) {
                        if (!PlayerData.getBoolean("仓库." + this.PlayerGUIPage.get(p) + ".已解锁层数.4")) {
                            evt.setCancelled(true);
                        }
                    }
                    else if (Click >= 36 && Click <= 44 && !PlayerData.getBoolean("仓库." + this.PlayerGUIPage.get(p) + ".已解锁层数.5")) {
                        evt.setCancelled(true);
                    }
                }
                if (Click == 45) {
                    final int Page = this.PlayerGUIPage.get(p) - 1;
                    p.closeInventory();
                    if (Page != 0 && p.hasPermission("BoxPage." + Page)) {
                        this.PlayerOpenBox(p, this.PlayerGUIPage.get(p) - 1);
                    }
                }
                else if (Click == 53) {
                    p.closeInventory();
                    if (p.hasPermission("BoxPage." + (this.PlayerGUIPage.get(p) + 1))) {
                        p.closeInventory();
                        this.PlayerOpenBox(p, this.PlayerGUIPage.get(p) + 1);
                    }
                }
                else if (Click == 49) {
                    if (this.PlayerGUIJieSuoOK.get(p)) {
                        final int Page = this.PlayerGUIPage.get(p);
                        final int Hang = this.PlayerGUIHang.get(p);
                        p.closeInventory();
                        this.JieSuo(p, Page, Hang);
                        this.PlayerOpenBox(p, Page);
                    }
                    else {
                        p.sendMessage(String.valueOf(this.Prefix) + "§4Error:你还不能解锁仓库！");
                    }
                }
            }
        }
    }
    
    private void JieSuo(final Player p, final int Page, final int Hang) {
        final File F = new File(this.getDataFolder() + "/PlayerData/" + p.getName() + ".yml");
        final FileConfiguration PlayerData = YamlConfiguration.loadConfiguration(F);
        
        Double NeedMoney = 0.0;
        Double NeedPoints = 0.0;
        if(Hang == 1) {
        	NeedMoney = 100.0;
        }else if (Hang == 2) {
        	NeedMoney = 200.0;
		}else if (Hang == 3) {
			NeedMoney = 300.0;
		}else if (Hang == 4) {
			NeedMoney = 400.0;
		}else if (Hang == 5) {
			NeedMoney = 500.0;
		}
        MoneyAPI.removeMoneys(p.getUniqueId(), NeedMoney);
        FCoinsAPI.removeFCoins(p.getUniqueId(), NeedPoints);
        PlayerData.set("仓库." + Page + ".已解锁层数." + Hang, true);
        if (Hang == 5) {
            PlayerData.set("仓库." + Page + ".是否解锁完成", true);
        }
        this.saveconfig(F, PlayerData);
    }
    
    private void SavePlayerBox(final Player p, final Inventory I, final int Page) {
        final File F = new File(this.getDataFolder() + "/PlayerData/" + p.getName() + ".yml");
        final FileConfiguration PlayerData = (FileConfiguration)YamlConfiguration.loadConfiguration(F);
        for (int a = 0; a <= 44; ++a) {
            if (!PlayerData.getBoolean("仓库." + Page + ".是否解锁完成")) {
                if (a >= 0 && a <= 8) {
                    if (PlayerData.getBoolean("仓库." + Page + ".已解锁层数.1")) {
                        this.saveItemStackData(I, Page, a, PlayerData);
                    }
                }
                else if (a >= 9 && a <= 17) {
                    if (PlayerData.getBoolean("仓库." + Page + ".已解锁层数.2")) {
                        this.saveItemStackData(I, Page, a, PlayerData);
                    }
                }
                else if (a >= 18 && a <= 26) {
                    if (PlayerData.getBoolean("仓库." + Page + ".已解锁层数.3")) {
                        this.saveItemStackData(I, Page, a, PlayerData);
                    }
                }
                else if (a >= 27 && a <= 35) {
                    if (PlayerData.getBoolean("仓库." + Page + ".已解锁层数.4")) {
                        this.saveItemStackData(I, Page, a, PlayerData);
                    }
                }
                else if (a >= 36 && a <= 44 && PlayerData.getBoolean("仓库." + Page + ".已解锁层数.5")) {
                    this.saveItemStackData(I, Page, a, PlayerData);
                }
            }
            else {
                this.saveItemStackData(I, Page, a, PlayerData);
            }
        }
        this.saveconfig(F, PlayerData);
    }
    
    private void saveItemStackData(final Inventory I, final int Page, final int ItemStack, final FileConfiguration PlayerData) {
        if (I.getItem(ItemStack) != null) {
            PlayerData.set("物品." + Page + "." + ItemStack + ".保存", true);
            PlayerData.set("物品." + Page + "." + ItemStack + ".数据", I.getItem(ItemStack));
        }
    }
    
    private void PlayerOpenBox(final Player p, final int Page) {
        if (!p.hasPermission("BoxPage." + Page)) {
            p.sendMessage(String.valueOf(this.Prefix) + "§4错误:您的权限不足！§6请联系服务器管理员！");
            return;
        }
        final File PlayerDataFile = new File(this.getDataFolder() + "/PlayerData/" + p.getName() + ".yml");
        if (!PlayerDataFile.exists()) {
            if (!PlayerDataFile.getParentFile().exists()) {
                PlayerDataFile.getParentFile().mkdirs();
            }
            try {
                PlayerDataFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
                p.sendMessage(String.valueOf(this.Prefix) + "§4错误:您的数据文件生成失败！§6请联系服务器管理员！");
                return;
            }
        }
        final FileConfiguration PlayerData = (FileConfiguration)YamlConfiguration.loadConfiguration(PlayerDataFile);
        final Inventory Inve = this.getPlayerDataInventory(p, PlayerData, Page);
        if (Inve != null) {
            p.openInventory(Inve);
            this.PlayerGUI.put(p, Inve);
            this.PlayerGUIPage.put(p, Page);
        }
    }
    
    private Inventory getPlayerDataInventory(final Player p, final FileConfiguration Data, final int Page) {
        Inventory Inve = null;
        try {
            Inve = Bukkit.createInventory(null, 54, this.BL("&f[&b"+p.getName()+"&f]&7的仓库第&c"+String.valueOf(Page)+"&7页"));
            ItemStack Lock = this.CraftItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&b此格子暂未解锁", Arrays.asList(new String[] {
            		"&c还不能够存放东西哦"
            }));
            int Hang = 1;
            for (int a = 1; a <= 5; ++a) {
                if (!Data.getBoolean("仓库." + Page + ".已解锁层数." + a)) {
                    for (int Size = a * 9 - 1, MinSize = a * 9 - 9; MinSize <= Size; --Size) {
                        Inve.setItem(Size, Lock);
                    }
                }
                else {
                    ++Hang;
                }
            }
            this.PlayerGUIHang.put(p, Hang);
            this.PlayerGUIJieSuoOK.put(p, this.JieSuoOK(p, Page, Hang));
            ItemStack PreviousPage;
            ItemStack NextPage;
            ItemStack CloseGUI;
            ItemStack Unlock;
            int Hang1 = Integer.parseInt(new StringBuilder().append(this.PlayerGUIHang.get(p) - 1).toString());
            double NeedMoney = 0;
            double NeedPoints = 0;
            if(Hang == 1) {
            	NeedMoney = 100;
            }else if (Hang == 2) {
            	NeedMoney = 200;
    		}else if (Hang == 3) {
    			NeedMoney = 300;
    		}else if (Hang == 4) {
    			NeedMoney = 400;
    		}else if (Hang == 5) {
    			NeedMoney = 500;
    		}
            String Money = new StringBuilder().append(MoneyAPI.getMoneys(p.getUniqueId())).toString();
            
            String Points = new StringBuilder().append(FCoinsAPI.getFCoins(p.getUniqueId())).toString();
                PreviousPage = this.CraftItemStack(Material.PAPER, "&b上一页", Arrays.asList(new String[] {
                		"&f[&7左键点击&f]"
                }));
                NextPage = this.CraftItemStack(Material.PAPER, "&b下一页", Arrays.asList(new String[] {
                		"&f[&7左键点击&f]"
                }));
                CloseGUI = this.CraftItemStack(Material.BARRIER, "&b关闭仓库", Arrays.asList(new String[] {
                		"&f[&7左键点击&f]"
                }));
                if (Data.getBoolean("仓库." + Page + ".是否解锁完成")) {
                    Unlock = this.CraftItemStack(Material.REDSTONE_TORCH, "&c这一页仓库已完成解锁", Arrays.asList(new String[] {
                    		"&c此页已经完成解锁"
                    }));
                }else {
                    Unlock = this.CraftItemStack(Material.LEVER, "&6&l解锁按钮&f[&7点击解锁一行&f]", Arrays.asList(new String[] {
                            "&8--------------------"
                            , "&7解锁进度&f: &e"+Hang1+"&f/&a5"
                            , "&8--------------------"
                            , "&7&l* &9解锁需要&f: &e&l"+NeedMoney+" &9铜钱"
                            , "&7&l* &9解锁需要&f: &e&l"+NeedPoints+" &9鱼币"
                            , "&8--------------------"
                            , "&c你的当前铜钱&f: §e§l"+Money
                            , "&c你的当前鱼币&f: §e§l"+Points
                            , "&8--------------------"
                            , "&b是否有解锁的权限: &e&l"+new StringBuilder().append(p.hasPermission("BoxPage." + Page + "." + this.PlayerGUIHang.get(p))).toString()
                            , "&b是否可以解锁: &e&l"+new StringBuilder().append(this.PlayerGUIJieSuoOK.get(p)).toString()
                    }));
                }
            Inve.setItem(49, Unlock);
            if (Page == 1) {
                Inve.setItem(45, CloseGUI);
            }
            else if (p.hasPermission("BoxPage." + (Page - 1))) {
                Inve.setItem(45, PreviousPage);
            }
            if (!p.hasPermission("BoxPage." + (Page + 1))) {
                Inve.setItem(53, CloseGUI);
            }
            else {
                Inve.setItem(53, NextPage);
            }
            for (int a = 0; a <= 44; ++a) {
                if (Data.getBoolean("物品." + Page + "." + a + ".保存")) {
                    final ItemStack Item = Data.getItemStack("物品." + Page + "." + a + ".数据");
                    Inve.setItem(a, Item);
                }
                Data.set("物品." + Page + "." + a, (Object)null);
            }
            this.saveconfig(new File(this.getDataFolder() + "/PlayerData/" + p.getName() + ".yml"), Data);
        }
        catch (NullPointerException e) {
            Bukkit.getConsoleSender().sendMessage(String.valueOf(this.Prefix) + "§4错误:§3[§6" + p.getName() + "§3]§4在执行§3[§6getPlayerDataInventory方法§3]§4的时候发生空指针错误!请将配置文件转为utf-8");
            Bukkit.getConsoleSender().sendMessage(String.valueOf(this.Prefix) + "§4提示:请检查§6Config.yml§4是否有配置错误!");
            p.sendMessage(String.valueOf(this.Prefix) + "§4错误:插件发生空指针错误！§6请将配置文件转为UTF-8");
            Bukkit.getConsoleSender().sendMessage(String.valueOf(this.Prefix) + "§4错误信息:§6↓↓↓");
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(String.valueOf(this.Prefix) + "§4错误信息:§6↑↑↑");
            Inve = null;
        }
        return Inve;
    }
    
    private String BL(final String str) {
        return str.replace("&", "§");
    }
    
    private static boolean isHexStr(final String str) {
        for (int i = 0; i < str.length(); ++i) {
            final char ch = str.charAt(i);
            final boolean isHex = (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
            if (!isHex) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isUnicode(final String unicodeStr, final int i) {
        final int len = unicodeStr.length();
        final int remain = len - i;
        if (remain < 5) {
            return false;
        }
        final char flag2 = unicodeStr.charAt(i + 1);
        if (flag2 != 'u') {
            return false;
        }
        final String nextFour = unicodeStr.substring(i + 2, i + 6);
        return isHexStr(nextFour);
    }
    
    private ItemStack CraftItemStack(final Material ID, final String Name, final List<String> Lore) {
        final ItemStack Item = new ItemStack(ID, 1);
        final ItemMeta Meta = Item.getItemMeta();
        if (Name != null) {
              Meta.setDisplayName(this.BL(Name));
        }
        if (Lore != null) {
            final ArrayList<String> LORE = new ArrayList<String>();
            for (int a = 0; a < Lore.size(); ++a) {
                    LORE.add(this.BL(Lore.get(a)));
            }
            Meta.setLore(LORE);
        }
        Item.setItemMeta(Meta);
        return Item;
    }
    
    private boolean JieSuoOK(Player p, int Page, int Hang) {
        double NeedMoney = 0;
        double NeedPoints = 0;
        if(Hang == 1) {
        	NeedMoney = 100;
        }else if (Hang == 2) {
        	NeedMoney = 200;
		}else if (Hang == 3) {
			NeedMoney = 300;
		}else if (Hang == 4) {
			NeedMoney = 400;
		}else if (Hang == 5) {
			NeedMoney = 500;
		}
        final double PlayerMoney = MoneyAPI.getMoneys(p.getUniqueId());
        final double PlayerPoints = FCoinsAPI.getFCoins(p.getUniqueId());
        return Hang != 6 && PlayerMoney >= NeedMoney && PlayerPoints >= NeedPoints && p.hasPermission("BoxPage." + Page + "." + Hang);
    }
}