package br.com.copacol.sapticket.service.redis;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import br.com.copacol.sapticket.web.dto.ProcessIdDTO;
import br.com.copacol.sapticket.web.dto.RedisContextDTO;
import tools.jackson.databind.ObjectMapper;

@Component
public class RedisStatus {
    // @Autowired
    // private RedisTemplate<String, Object> redisTemplate;

    // public void salvar(String key, Object valor) {
    // redisTemplate.opsForValue().set(key, valor, Duration.ofMinutes(60));
    // }

    // public Object buscar(String key) {
    // return redisTemplate.opsForValue().get(key);
    // }

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisStatus(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void salvar(String key, RedisContextDTO valor) {
        try {
            String json = objectMapper.writeValueAsString(valor);
            redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(60));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao serializar status", e);
        }
    }

    public RedisContextDTO buscar(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            return json != null ? objectMapper.readValue(json, RedisContextDTO.class) : null;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar status", e);
        }
    }

    // public void salvarSessaoPorProcessId(String sessao, List<String> processId){
    // try {
    // // String json = objectMapper.writeValueAsString(processId);
    // redisTemplate.opsForValue().set(sessao, processId, Duration.ofMinutes(60));
    // } catch (Exception e) {
    // e.printStackTrace();
    // throw new RuntimeException("Erro ao serializar status", e);
    // }
    // }

    public void adicionarProcessIdNaSessao(String sessao, String processId) {
        redisTemplate.opsForList().rightPush("sessao:processos:" + sessao, processId);
        redisTemplate.expire("sessao:processos:" + sessao, Duration.ofMinutes(60));
    }

    public List<String> buscarProcessIdsDaSessao(String sessao) {
        return redisTemplate.opsForList().range("sessao:processos:" + sessao, 0, -1);
    }

    public ProcessIdDTO findConversationByProcessId(String processId){
         String json = redisTemplate.opsForValue().get(processId);
        return json != null ? objectMapper.readValue(json, ProcessIdDTO.class) : null;
    }
}
