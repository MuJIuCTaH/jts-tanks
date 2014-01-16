package ru.jts.server.network.serverpackets;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ru.jts.common.network.udp.ServerPacket;
import ru.jts.common.util.ArrayUtils;
import ru.jts.server.network.Client;
import ru.jts.server.network.crypt.CryptEngine;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Camelion
 * @date: 20.12.13/1:36
 */
public class AuthorizeResponse extends ServerPacket<Client> {
    private final short sessionId;
    private ByteBuf buf;

    public AuthorizeResponse(short sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    protected void before() throws Exception {
        buf = Unpooled.buffer().order(ByteOrder.LITTLE_ENDIAN);

        InetSocketAddress address = getClient().getServerAddress();

        buf.writeBytes(address.getAddress().getAddress());
        buf.writeInt(address.getPort());

        buf.writeInt(getClient().getRandomKey());

        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("security_msg", "old_pass");
        jsonMap.put("token2", getClient().generateToken2());

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(jsonMap);
        buf.writeByte(json.length());
        buf.writeBytes(json.getBytes());

        byte[] cryptedData = CryptEngine.getInstance().encrypt(buf.copy().array(), getClient().getBlowFishKey(), CryptEngine.ZERO_TRAILING_MODE);

        buf.clear();
        buf.writeBytes(cryptedData);
    }

    @Override
    protected void writeImpl() {
        writeShort(0x00);
        writeByte(0xFF);
        writeByte(0x7D);
        writeBytes(0x00, 0x00, 0x00);
        writeShort(sessionId);
        writeByte(0x00); // 0x01 есть открытая сессия 0x00 - нету
        writeBytes(0x00, 0x01);
        writeBytes(buf);

        System.out.println(ArrayUtils.bytesToHexString(getClient().getBlowFishKey()));
        System.out.println(ArrayUtils.bytesToHexString(content.copy().array()));
        // crypted with CryptEngine
        // writeBytes(127, 0, 0, 1); // доп. ип игрового сервера
        // writeInt(12313); // открытый для клиента порт игрового сервера
        // writeInt(unk); 68 19 3F 5F // проверочный ключ, рандомный, клиент отсылает его обратно в следующем пакете
        // writeShort(json msg size);
        // writeString(json msg); {"security_msg":"old_pass","token2":"8531071:5661541700570227003:134709503890988403063748623096524483879"} 
        // writeByte(0x00);
    }
}
