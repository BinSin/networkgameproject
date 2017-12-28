package chat.server;
// Java Chatting Server

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JFrame;

import chatViusal.ChatVisual;
import kr.ac.hansung.game.Game;
import kr.ac.hansung.game.GameLauncher;

public class ChatServer  {

	private ServerSocket socket; // server socket
	private Socket soc;//connection socket
	private int port;//port number
	private Vector vc = new Vector();
	
	//private Game game;
	
	
	public ChatServer() {
		
		try {
			this.socket = new ServerSocket(1331);
			if(socket!=null)//nomally open socket  
			{
				connection();
				
			}
		}catch (IOException e) {
			
		}
	}

	public void connection() {
		Thread th = new Thread(new Runnable(){ // 사용자 접속을 받을 스레드

			@Override
			public void run() {
				while(true) {//사용자 접속을 계속해서 받기 위해 while문
					try {
						soc = socket.accept();//accept가 일어나기 전까지는 무한 대기중
						ChatVisual.chatScreen.append("user connect\n");
						UserInfo user = new UserInfo(soc, vc);
						
						vc.add(user);// add object into vector
						user.start();
					}catch(IOException e) {
						ChatVisual.chatScreen.append("!!!!!! accept error... !!!!!");
					}
					
				}
				
			}
			
		});
		th.start();
	}
	
	class UserInfo extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Socket user_socket;
		private Vector user_vc;
		private String Nickname = "";

		public UserInfo(Socket soc, Vector vc) // 생성자메소드
		{
			// 매개변수로 넘어온 자료 저장
			this.user_socket = soc;
			this.user_vc = vc;
			User_network();
		}
		public void User_network() {
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);
				//Nickname = dis.readUTF(); // 사용자의 닉네임 받는부분
				byte[] b=new byte[128];
				dis.read(b);
				String Nickname = new String(b);
				Nickname = Nickname.trim();
				ChatVisual.chatScreen.append("ID " + Nickname + " connect\n");
				ChatVisual.chatScreen.setCaretPosition(ChatVisual.chatScreen.getText().length());		
				send_Message(Nickname + "님 Hello~!."); // 연결된 사용자에게 정상접속을 알림
			} catch (Exception e) {
				ChatVisual.chatScreen.append("Stream setting error\n");
				ChatVisual.chatScreen.setCaretPosition(ChatVisual.chatScreen.getText().length());
			}
		}

		public void InMessage(String str) {
			//textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
			ChatVisual.chatScreen.append(str + "\n");
			ChatVisual.chatScreen.setCaretPosition(ChatVisual.chatScreen.getText().length());
			// 사용자 메세지 처리
			broad_cast(str);
		}

		public void broad_cast(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo imsi = (UserInfo) user_vc.elementAt(i);
				imsi.send_Message(str);
			}
		}

		public void send_Message(String str) {
			try {
				//dos.writeUTF(str);
				byte[] bb;		
				bb = str.getBytes();
				dos.write(bb); //.writeUTF(str);
			} 
			catch (IOException e) {
				ChatVisual.chatScreen.append("메시지 송신 에러 발생\n");	
				ChatVisual.chatScreen.setCaretPosition(ChatVisual.chatScreen.getText().length());
			}
		}

		public void run() // 스레드 정의
		{

			while (true) {
				try {
					
					// 사용자에게 받는 메세지
					byte[] b = new byte[128];
					dis.read(b);
					String msg = new String(b);
					msg = msg.trim();
					//String msg = dis.readUTF();
					InMessage(msg);
					
				} 
				catch (IOException e) 
				{
					
					try {
						dos.close();
						dis.close();
						user_socket.close();
						vc.removeElement( this ); // 에러가난 현재 객체를 벡터에서 지운다
						ChatVisual.chatScreen.append(vc.size() +" : current vector user Number\n");
						ChatVisual.chatScreen.append("user disconnect data return \n");
						ChatVisual.chatScreen.setCaretPosition(ChatVisual.chatScreen.getText().length());
						
						break;
					
					} catch (Exception ee) {
					
					}// catch문 끝
				}// 바깥 catch문끝

			}
			
			
			
		}// run메소드 끝

	} // 내부 userinfo클래스끝

	public void start() {
		// TODO Auto-generated method stub
		
	}

}
