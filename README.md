# abastecIA ⛽💳

O **abastecIA** é o aplicativo nativo Android desenvolvido em Kotlin e Jetpack Compose para automação operacional de postos de combustível. 

Ele é projetado para rodar **diretamente nos Smart POS (terminais inteligentes de pagamento/maquininhas)** do PagBank. Ao receber comandos de pagamento em tempo real a partir de uma fila de atendimento sincronizada no **Supabase**, o frentista pode iniciar cobranças físicas de forma direta e integrada no terminal via SDK **PlugPag**, atualizando o status de volta no banco de dados automaticamente.

---

## 🚀 Guia de Integração com o Supabase

O aplicativo depende do **Supabase Realtime** para receber notificações instantâneas de novos pagamentos acionados pela web e do **Supabase Postgrest** para ler e atualizar as transações diretamente.

Siga os quatro passos simples abaixo para conectar o projeto ao seu banco de dados Supabase:

### Passo 1: Criação da Tabela no Banco de Dados

Acesse o **SQL Editor** do seu painel do Supabase e execute o seguinte script para criar a tabela de pedidos de abastecimento (`fuel_orders`) e configurar seus índices de performance:

```sql
-- Criar tabela de pedidos de combustível
CREATE TABLE fuel_orders (
    id TEXT PRIMARY KEY,                       -- UUID gerado na criação ou ID vindo da web
    company_id TEXT NOT NULL,                 -- ID identificador do posto de combustível
    pump_number TEXT NOT NULL,                -- Número da bomba (ex: "03")
    fuel_type TEXT NOT NULL,                 -- Tipo de combustível (ex: "Gasolina Aditivada")
    amount NUMERIC(10, 2) NOT NULL,           -- Valor total a ser cobrado
    status TEXT NOT NULL DEFAULT 'paid',      -- Status da transação: 'paid' (aguardando cobrança), 'paid_machine' (pago na maquininha), 'done' (finalizado)
    plate TEXT,                               -- Placa do carro (ex: "BRA2E19")
    payment_method TEXT DEFAULT 'card',       -- Método de pagamento padrão
    plugpag_nsu TEXT,                         -- NSU retornado pela maquininha PagBank
    plugpag_auth TEXT,                        -- Código de autorização do PagBank
    plugpag_card_last4 TEXT,                  -- Últimos 4 dígitos do cartão do cliente
    plugpag_installments INT DEFAULT 1,       -- Quantidade de parcelas da transação
    paid_at TEXT,                             -- Timestamp textual do momento da aprovação
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Habilitar Índices para aceleração de buscas no frentista
CREATE INDEX idx_orders_company_status ON fuel_orders (company_id, status);
```

### Passo 2: Habilitar o Supabase Realtime

Para que o aplicativo seja notificado instantaneamente quando novos registros forem inseridos ou alterados na plataforma web (Supabase Realtime), você precisa habilitar a replicação em tempo real para a tabela criada:

1. Acesse o painel do **Supabase**.
2. No menu lateral esquerdo, vá em **Database**.
3. Clique em **Replication**.
4. Procure pela publicação padrão chamada `supabase_realtime`.
5. Selecione **Tables** (ou clique para editar a replicação).
6. Alterne o interruptor para habilitar a tabela `fuel_orders` nas transmissões Realtime.

> **💡 Dica Técnica:** Se preferir, você também pode ativar isso diretamente via SQL executando o comando abaixo:
> ```sql
> alter publication supabase_realtime add table fuel_orders;
> ```

### Passo 3: Configurar as Credenciais no Aplicativo

O painel de **Ajustes** do abastecIA permite que o gerente ou frentista conecte o terminal ao banco apropriado sem precisar recompilar o aplicativo.

No aplicativo, mude para a aba **Ajustes** (ícone de engrenagem) e insira:

1. **URL do Banco (Supabase URL)**: Obtida em *Project Settings -> API -> Project URL* (ex: `https://vossosubabase.supabase.co`).
2. **Proxy/Anon Public Key**: Token público de acesso do Supabase em *Project Settings -> API -> `anon` public key*.
3. **ID do Posto (company_id)**: Identificador único que separa os pedidos de postos diferentes (ex: `posto_sul_01`).

---

## ⚡ Fluxos Operacionais & Mudança de Status

O fluxo típico de um atendimento segue a máquina de estados abaixo:

```
[ Cliente Web / Celular ]
      │ (Cria pedido automatizado)
      ▼
Status: 'paid' ──► Recebido via Supabase Realtime pelo App Smart POS
                      │
                      ├─► Frentista clica no pedido no painel
                      ├─► Insere/aproxima cartão no leitor integrado da máquina PagBank
                      │
                      ▼ (Verificado e aprovado pelo PlugPag SDK)
Status: 'paid_machine' ──► Sincroniza novos dados (NSU, auth, parcelas, paid_at) no Supabase
                      │
                      ├─► Combustível abastecido pelo frentista
                      ├─► Clientes liberados
                      ├─► Frentista pressiona "Concluir" no App
                      │
                      ▼
Status: 'done' ──► Removido da fila de atendimento pendente
```

---

## 🛠️ Simulação e Testes Rápidos

Caso queira demonstrar ou testar o aplicativo em um ambiente sem internet ou sem as credenciais do Supabase configuradas, você pode ir na tela de **Ajustes** e ligar o interruptor de **Demonstração Offline / Simulação**.

Com o simulador ativo:
* É possível rodar fluxos de transação completos sem credenciais.
* Use o botão **Simular Pedido** na barra superior do frentista para criar pedidos aleatórios instantaneamente.

---

## 🤖 Como Alterar o Provedor de IA (Gemini para ChatGPT / Groq)

O ecossistema **abastecIA** utiliza Inteligência Artificial na camada web/servidor para interpretar comandos de voz dos motoristas ou frentistas (ex: *"Coloca 100 de aditivada no Prisma placa BRA2E19 na bomba 3"*) e convertê-los de forma estruturada no JSON esperado pelo banco do Supabase.

Se no seu backend (Vercel Functions, Node.js ou FastAPI) você deseja alternar o processamento de linguagem natural do **Google Gemini** para **Groq** ou **OpenAI (ChatGPT)**, aplique as substituições de código abaixo:

### 1. Se estiver utilizando OpenAI (ChatGPT)
Se deseja usar os modelos `gpt-4o` ou `gpt-3.5-turbo`, substitua o parser do Gemini por:

```javascript
import OpenAI from "openai";

const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY // Chave do ChatGPT
});

async function parseVoicePrompt(promptText) {
  const response = await openai.chat.completions.create({
    model: "gpt-4o-mini", // ou "gpt-4o"
    messages: [
      {
        role: "system",
        content: "Você é um assistente de posto. Extraia as informações no formato JSON com as chaves: pump_number (string), fuel_type (string, opções: Gasolina Comum, Gasolina Aditivada, Etanol Comum, Diesel S10), amount (number) e plate (string ou null)."
      },
      { role: "user", content: promptText }
    ],
    response_format: { type: "json_object" } // Garante retorno JSON válido
  });

  return JSON.parse(response.choices[0].message.content);
}
```

### 2. Se estiver utilizando Groq (Llama 3 / Mixtral)
O Groq é extremamente veloz e possui custos menores. Substitua o parser para:

```javascript
import Groq from "groq-sdk";

const groq = new Groq({
  apiKey: process.env.GROQ_API_KEY // Chave do Groq Console
});

async function parseVoicePrompt(promptText) {
  const chatCompletion = await groq.chat.completions.create({
    model: "llama3-8b-8192", // Modelo de alta performance e baixíssima latência
    messages: [
      {
        role: "system",
        content: "Você é um parser do posto abastecIA. Extraia dados em formato JSON com chaves: pump_number (string), fuel_type, amount, plate."
      },
      { role: "user", content: promptText }
    ],
    response_format: { type: "json_object" }
  });

  return JSON.parse(chatCompletion.choices[0].choices[0].message.content);
}
```

### 3. Onde configurar as variáveis de ambiente?
* Se for hospedar a página web e as APIs no **Vercel**, adicione as chaves em **Settings -> Environment Variables** de seu projeto:
  * Para OpenAI: `OPENAI_API_KEY`
  * Para Groq: `GROQ_API_KEY`
* Se estiver testando localmente, crie ou altere seu arquivo `.env`:
  ```env
  # Exemplo de configuração local
  GROQ_API_KEY=gsk_vossa_chave_aqui
  # ou
  OPENAI_API_KEY=sk-proj-vossa_chave_aqui
  ```
