package me.polymarsdev.teamsnake.entity;

import java.io.Serializable;

public class Tile implements Serializable {
    //snake modes
    final int CLASSIC = 0;
    final int GLUTTED = 1;
    final int CHARGED = 2;
    final int DEVIL = 3;
    //apple modes
    final int SINGLE = 0;
    final int PORTAL = 1;
    //directions
    final int DEADSEGMENT = 8;
    final int DEADENDSEGMENT = 7;
    final int ENDSEGMENT = 6;
    final int YUMHEAD = 5;
    final int DEADHEAD = 4;
    final int HEAD = 3;
    final int APPLE = 2;
    final int SEGMENT = 1;
    final int GROUND = 0;
    int status = 0;
    int skin = 0;
    public Tile(int status, int skin)
    {
        this.status = status;
        this.skin = skin;
    }
    public void setStatus(int status)
    {
        this.status = status;
    }
    public int getStatus()
    {
        return this.status;
    }
    public String toString()
    {
        if (status == SEGMENT)
        {
            switch (skin)
            {
                case DEVIL:
                    return "\uD83D\uDFEA";
                case GLUTTED:
                    return "\uD83D\uDFE9";
                case CHARGED:
                    return "\uD83D\uDFE5";
                default:
                    return "\uD83D\uDFE8";
            }

        }
        else if (status == ENDSEGMENT)
        {
            switch (skin)
            {
                case DEVIL:
                    return "\uD83D\uDFE3";
                case GLUTTED:
                    return "\uD83D\uDFE2";
                case CHARGED:
                    return "\uD83D\uDD34";
                default:
                    return "\uD83D\uDFE1";
            }
        }
        else if (status == HEAD)
        {
            switch (skin)
            {
                case DEVIL:
                    return "\uD83D\uDE08";
                case GLUTTED:
                    return "\uD83E\uDD22";
                case CHARGED:
                    return "\uD83D\uDE21";
                default:
                    return "\uD83D\uDE33";
            }
        }
        else if (status == DEADHEAD)
        {
            return "\uD83D\uDE35";
        }
        else if (status == DEADSEGMENT)
        {
            return "\uD83D\uDFE8";
        }
        else if (status == DEADENDSEGMENT)
        {
            return "\uD83D\uDFE1";
        }
        else if (status == YUMHEAD)
        {
            return "\uD83D\uDE29";
        }
        else if (status == APPLE)
        {
            return "\uD83C\uDF4E";
        }
        return ":black_large_square:";
    }
}