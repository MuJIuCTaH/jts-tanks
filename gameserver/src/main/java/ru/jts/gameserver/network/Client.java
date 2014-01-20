/*
 * Copyright 2014 jts
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.jts.gameserver.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import ru.jts.common.math.Rnd;
import ru.jts.common.network.IClient;
import ru.jts.common.network.udp.Auth2ClientServerPacket;
import ru.jts.common.network.udp.IUDPServerPacketHandler;
import ru.jts.gameserver.network.handler.Game2ClientPacketHandler;

import java.net.InetSocketAddress;

/**
 * @author: Camelion
 * @date: 02.11.13/16:20
 * <p/>
 */
public class Client implements IClient {

	private Channel channel;
	private IUDPServerPacketHandler<Client> packetHandler;
	private byte[] blowFishKey;
	private int randomKey;
	private String token2;
	private InetSocketAddress myAddress;

	public Client(InetSocketAddress myAddress, Channel channel) {
		this.channel = channel;
		this.packetHandler = Game2ClientPacketHandler.getInstance();
		this.myAddress = myAddress;
	}

	public IUDPServerPacketHandler<Client> getPacketHandler() {
		return packetHandler;
	}

	public void sendPacket(Auth2ClientServerPacket packet) {
		packet.setClient(this);
		packet.write();

		ByteBuf buf = packet.getContent();
		buf = getPacketHandler().encrypt(buf);

		channel.writeAndFlush(new DatagramPacket(buf, myAddress));
	}

	public byte[] getBlowFishKey() {
		return blowFishKey;
	}

	public void setBlowFishKey(byte[] blowFishKey) {
		this.blowFishKey = blowFishKey;
	}

	public int getRandomKey() {
		return randomKey;
	}

	public void setRandomKey(int randomKey) {
		this.randomKey = randomKey;
	}

	public String generateToken2() {
		StringBuilder token = new StringBuilder();
		for (int i = 0; i < 39; i++) {
			token.append(Rnd.nextDigest());
		}

		return token2 = "1234567" + ":" + "1234567890123456789" + ":" + token;
	}
}
