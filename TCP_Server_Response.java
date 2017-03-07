package servers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JOptionPane;

import MySQL.Function;

public class TCP_Server_Response {
	static int port = 8081;
	String key;

	public static void main(String[] args) throws IOException {
		Server();
	}

	public static String getMac(String ip) {
		try {

			ProcessBuilder Builder = new ProcessBuilder("arp", "-a");
			Builder.redirectErrorStream(true);
			Process process;
			process = Builder.start();
			InputStream is = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			String ligne;
			while ((ligne = reader.readLine()) != null) {
				if (ligne.contains(ip)) {
					ligne = ligne.replaceAll(ip, "").replaceAll("dynamique", "").replaceAll("static", "")
							.replaceAll("ether", "").replaceAll("on", "").replaceAll("eth0", "").replaceAll(" ", "")
							.replaceAll("\\?", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("at", "")
							.replaceAll("\\[", "").replaceAll("\\]", "");
					break;
				}

			}
			return ligne;
		} catch (IOException e1) {
			return "Error";
		}

	}

	public static void Server() {

		while (true) {

			try {
				ServerSocket s;
				System.out.println(InetAddress.getLocalHost() + " écoute du port : " + port + "...");
				s = new ServerSocket(port);
				Socket soc = s.accept();

				BufferedReader plec = new BufferedReader(new InputStreamReader(soc.getInputStream()));
				PrintWriter pred = new PrintWriter(new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())),
						true);

				String str = plec.readLine();
				System.out.println(soc + " Message : " + str);
				// pred.println("M");

				String IP = soc.getInetAddress().toString().replace("/", "");
				// IP = IP.replace("/", "");
				System.out.println(IP);

				String Mac = getMac(IP);

				Function f = new Function("192.168.12.192", "root", "raspberry", "cveso");

				/*
				 * String[] r = str.split(" : "); String id = r[1]; String[]
				 * result = f.Find(id); if (result[0].equals("")) { // refuse
				 * pred.println("Autorisation !OK"); } else {
				 * System.out.println("Nom : " + result[1] + "Prenom : " +
				 * result[2]); // valide l'entré
				 * pred.println("Autorisation OK"); } else {
				 * pred.println("Check Server OK"); }
				 */

				if (str.equals("Check_connection...")) {

					boolean isPresent = f.Trykey(Mac);
					System.out.println("isPresent " + Mac + " : " + isPresent);

					if (!isPresent) {

						System.out.println("MAC non présent. 0 to add :");
						Scanner in = new Scanner(System.in);
						int retval = in.nextInt();

						/*
						 * int retval = JOptionPane.showConfirmDialog(null,
						 * "Nouvelle adresse MAC détectée voulez l'ajouter :" +
						 * Mac, "Ajout MAC ?", JOptionPane.OK_CANCEL_OPTION);
						 */

						if (retval == 0) {
							System.out.println("Ajout en cours");
							f.addKey(Mac);
							pred.println("Connection_granted");
						} else {
							pred.println("Connection_denied");
						}

					} else if (isPresent) {
						pred.println("Connection_granted");
					}

				} else if (str.contains("Auth_RFID")) {
					// TODO RFID
					String[] r = str.split(" : ");
					String id = r[1];
					String[] result = f.Find("Id-RFID", id);
					if (result[0].equals("")) { // refuse
						pred.println("Access_denied");
					} else {
						System.out.println("Nom : " + result[1] + "Prenom : " + result[2]);
						pred.println("Access_granted : Nom : " + result[1] + " : Prenom : " + result[2]);
					}
				}

				else if (str.equals("Auth_PUCE")) {
					// TODO PUCE
				}

				else if (str.equals("Auth_BIO")) {
					// TODO BIO
				}

				plec.close();
				pred.close();
				soc.close();
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
