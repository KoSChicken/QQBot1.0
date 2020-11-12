package io.koschicken.bean;

public class GroupPower {
    private boolean globalSwitch;//总开关
    private boolean gachaSwitch;//扭蛋开关
    private boolean maiyaoSwitch;//买药小助手提示
    private boolean horseSwitch;//赛马开关
    private boolean setuSwitch;
    private boolean diceSwitch;

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

    public boolean isSetuSwitch() {
        return setuSwitch;
    }

    public void setSetuSwitch(boolean setuSwitch) {
        this.setuSwitch = setuSwitch;
    }

    public boolean isDiceSwitch() {
        return diceSwitch;
    }

    public void setDiceSwitch(boolean diceSwitch) {
        this.diceSwitch = diceSwitch;
    }

    @Override
    public String toString() {
        return "GroupPower{" +
                "globalSwitch=" + globalSwitch +
                ", gachaSwitch=" + gachaSwitch +
                ", maiyaoSwitch=" + maiyaoSwitch +
                ", horseSwitch=" + horseSwitch +
                ", setuSwitch=" + setuSwitch +
                ", diceSwitch=" + diceSwitch +
                '}';
    }
}
