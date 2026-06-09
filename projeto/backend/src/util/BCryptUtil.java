package util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

// Responsavel por gerar e verificar hash de senha
// Usa SHA-256 com um salt aleatorio pra cada usuario
// Formato salvo no banco: base64(salt):base64(hash)
public class BCryptUtil {

    //SECURERANDOW MELHOR QUE RANDOW EM QUESTAO DE SEGURANCA
    private static final SecureRandom RANDOM = new SecureRandom();

    // Gera o hash da senha com salt aleatorio
    // O salt faz com que dois usuarios com a mesma senha tenham hashes diferentes
    public static String hash(String senha) {
        try {
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt); // gera 16 bytes aleatorios

            byte[] hashBytes = sha256(senha, salt);

            // Salva como "salt:hash" ambos em Base64
            return Base64.getEncoder().encodeToString(salt) +
                   ":" +
                   Base64.getEncoder().encodeToString(hashBytes);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash da senha", e);
        }
    }

    // Verifica se a senha confere com o hash salvo no banco
    public static boolean verificar(String senha, String hashSalvo) {
        try {
            // Separa o salt do hash pelo ":"
            String[] partes = hashSalvo.split(":");
            if (partes.length != 2) return false;

            byte[] salt     = Base64.getDecoder().decode(partes[0]);
            byte[] esperado = Base64.getDecoder().decode(partes[1]);

            // Recalcula o hash com o mesmo salt
            byte[] calculado = sha256(senha, salt);

            // Compara byte a byte
            if (esperado.length != calculado.length) return false;

            int diferenca = 0;
            for (int i = 0; i < esperado.length; i++) {
                diferenca |= esperado[i] ^ calculado[i]; // XOR: 0 se iguais
            }

            return diferenca == 0;

        } catch (Exception e) {
            return false;
        }
    }

    // Aplica SHA-256 misturando salt + senha
    private static byte[] sha256(String senha, byte[] salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        return md.digest(senha.getBytes("UTF-8"));
    }
}
