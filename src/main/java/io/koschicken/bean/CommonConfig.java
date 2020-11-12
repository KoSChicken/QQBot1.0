package io.koschicken.bean;

public class CommonConfig {
    // 提醒买药小助手文件名
    private String maiyaoPic;
    // 抽卡上限
    private int gachaLimit;
    //抽卡冷却时间，以秒为单位
    private int gachaCooldown;
    //总开关
    private boolean globalSwitch;
    //扭蛋开关
    private boolean gachaSwitch;
    //买药小助手提示
    private boolean maiyaoSwitch;
    //赛马开关
    private boolean horseSwitch;
    //主人qq
    private String masterQQ;
    //签到一次给的钱
    private int signCoin;
    //发一次图给的钱
    private int setuCoin;
    //r18图片的私聊开关
    private boolean r18Private;

    public CommonConfig() {
    }

    public CommonConfig(String maiyaoPic, int gachaLimit, int gachaCooldown,
                        boolean globalSwitch, boolean gachaSwitch, boolean maiyaoSwitch, boolean horseSwitch,
                        String masterQQ, int signCoin, int setuCoin, boolean r18Private) {
        this.maiyaoPic = maiyaoPic;
        this.gachaLimit = gachaLimit;
        this.gachaCooldown = gachaCooldown;
        this.globalSwitch = globalSwitch;
        this.gachaSwitch = gachaSwitch;
        this.maiyaoSwitch = maiyaoSwitch;
        this.horseSwitch = horseSwitch;
        this.masterQQ = masterQQ;
        this.signCoin = signCoin;
        this.setuCoin = setuCoin;
        this.r18Private = r18Private;
    }

    public String getMaiyaoPic() {
        return maiyaoPic;
    }

    public void setMaiyaoPic(String maiyaoPic) {
        this.maiyaoPic = maiyaoPic;
    }

    public int getGachaLimit() {
        return gachaLimit;
    }

    public void setGachaLimit(int gachaLimit) {
        this.gachaLimit = gachaLimit;
    }

    public int getGachaCooldown() {
        return gachaCooldown;
    }

    public void setGachaCooldown(int gachaCooldown) {
        this.gachaCooldown = gachaCooldown;
    }

    public boolean isGlobalSwitch() {
        return globalSwitch;
    }

    public void setGlobalSwitch(boolean globalSwitch) {
        this.globalSwitch = globalSwitch;
    }

    public boolean isGachaSwitch() {
        return gachaSwitch;
    }

    public void setGachaSwitch(boolean gachaSwitch) {
        this.gachaSwitch = gachaSwitch;
    }

    public boolean isMaiyaoSwitch() {
        return maiyaoSwitch;
    }

    public void setMaiyaoSwitch(boolean maiyaoSwitch) {
        this.maiyaoSwitch = maiyaoSwitch;
    }

    public boolean isHorseSwitch() {
        return horseSwitch;
    }

    public void setHorseSwitch(boolean horseSwitch) {
        this.horseSwitch = horseSwitch;
    }

    public String getMasterQQ() {
        return masterQQ;
    }

    public void setMasterQQ(String masterQQ) {
        this.masterQQ = masterQQ;
    }

    public int getSignCoin() {
        return signCoin;
    }

    public void setSignCoin(int signCoin) {
        this.signCoin = signCoin;
    }

    public int getSetuCoin() {
        return setuCoin;
    }

    public void setSetuCoin(int setuCoin) {
        this.setuCoin = setuCoin;
    }

    public boolean isR18Private() {
        return r18Private;
    }

    public void setR18Private(boolean r18Private) {
        this.r18Private = r18Private;
    }

    @Override
    public String toString() {
        return "CommonConfig{" +
                "maiyaoPic='" + maiyaoPic + '\'' +
                ", gachaLimit=" + gachaLimit +
                ", gachaCooldown=" + gachaCooldown +
                ", globalSwitch=" + globalSwitch +
                ", gachaSwitch=" + gachaSwitch +
                ", maiyaoSwitch=" + maiyaoSwitch +
                ", horseSwitch=" + horseSwitch +
                ", masterQQ='" + masterQQ + '\'' +
                ", signCoin=" + signCoin +
                ", setuCoin=" + setuCoin +
                ", r18Private=" + r18Private +
                '}';
    }
}
