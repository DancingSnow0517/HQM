package hardcorequesting.network.message;

import hardcorequesting.network.GeneralUsage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class GeneralUpdateMessage implements IMessage, IMessageHandler<GeneralUpdateMessage, IMessage>{
    
    private EntityPlayer player;
    private NBTTagCompound data;
    private int usage = -1;
    
    public GeneralUpdateMessage(){}
    
    public GeneralUpdateMessage(EntityPlayer player, NBTTagCompound data, int usage){
        this.player = player;
        this.data = data;
        this.usage = usage;
    }
    
    @Override
    public void fromBytes(ByteBuf buf){
        int worldId = buf.readInt();
        UUID playerId = new PacketBuffer(buf).readUniqueId();
        this.data = ByteBufUtils.readTag(buf);
        this.usage = buf.readInt();
    
        World world = DimensionManager.getWorld(worldId);
        if(world != null){
            this.player = world.getPlayerEntityByUUID(playerId);
        }
    }
    
    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(this.player.getEntityWorld().provider.getDimension());
        new PacketBuffer(buf).writeUniqueId(this.player.getPersistentID());
        ByteBufUtils.writeTag(buf, this.data);
        buf.writeInt(this.usage);
    }
    
    @Override
    public IMessage onMessage(GeneralUpdateMessage message, MessageContext ctx){
        if(message.data != null && message.usage >= 0){
            GeneralUsage usage = GeneralUsage.values()[message.usage];
            if(message.player != null){
                if(message.player instanceof EntityPlayerMP){// message from client
                    usage.receiveData(message.player, message.data, Side.SERVER);
                } else {// message from server
                    usage.receiveData(message.player, message.data, Side.CLIENT);
                }
            }
        }
        return null;
    }
}
