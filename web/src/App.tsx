import { useState } from 'react';
import './App.css'; 

function App() {
  // Estado do Formulário 
  const [formData, setFormData] = useState({
    nomeResponsavel: '',
    emailContato: '',
    instituicao: '',
    dataVisita: '',
    horarioVisita: '',
    tipoGrupo: 'adultos',
    qtdPessoas: 1,
    mensagem: ''
  });

  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [loading, setLoading] = useState(false);

  // Função que atualiza os campos e valida em tempo real
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    
    // Limpa mensagens ao digitar
    setErro('');
    setSucesso('');

    // Validação de Dias da Semana (Terça=2, Quarta=3, Quinta=4)
    if (name === 'dataVisita') {
      const date = new Date(value);
      const day = date.getUTCDay(); // Usa UTC para evitar fuso horário errado
      
      if (day !== 2 && day !== 3 && day !== 4) {
        setErro('⚠️ Atenção: Visitas permitidas apenas Terças, Quartas e Quintas.');
      }
    }

    //Validação de Quantidade de Pessoas
    if (name === 'qtdPessoas' || name === 'tipoGrupo') {
      const tipo = name === 'tipoGrupo' ? value : formData.tipoGrupo;
      const qtd = name === 'qtdPessoas' ? Number(value) : formData.qtdPessoas;

      const max = tipo === 'criancas' ? 15 : 20;
      
      if (qtd > max) {
        setErro(`⚠️ Para grupos de ${tipo}, o máximo é de ${max} pessoas.`);
      }
    }

    setFormData({ ...formData, [name]: value });
  };

  // Função de Envio
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); 

    if (erro) return; 
    if (!formData.nomeResponsavel || !formData.emailContato || !formData.instituicao || !formData.dataVisita || !formData.horarioVisita) {
        setErro("Por favor, preencha todos os campos obrigatórios.");
        return;
    }

    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/email/solicitar-visita', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      if (response.ok) {
        setSucesso('✅ Solicitação enviada com sucesso! Verifique seu e-mail.');
        
        setFormData({
          nomeResponsavel: '',
          emailContato: '',
          instituicao: '',
          dataVisita: '',
          horarioVisita: '',
          tipoGrupo: 'adultos', 
          qtdPessoas: 1,       
          mensagem: ''
        });

      } else {
        setErro('❌ Erro ao enviar. Tente novamente mais tarde.');
      }
    } catch (error) {
      setErro('❌ Erro de conexão com o servidor.');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-container">
      <h2>♻️ Agendamento PUCRS</h2>
      
      {sucesso && <div className="success-msg">{sucesso}</div>}
      {erro && <div className="error-msg">{erro}</div>}

      <form onSubmit={handleSubmit}>
        <label>Nome do Responsável *</label>
        <input name="nomeResponsavel" value={formData.nomeResponsavel} onChange={handleChange} placeholder="Ex: Prof. Carlos Silva" required />

        <label>E-mail para Contato *</label>
        <input type="email" name="emailContato" value={formData.emailContato} onChange={handleChange} placeholder="email@exemplo.com" required />

        <label>Instituição / Escola *</label>
        <input name="instituicao" value={formData.instituicao} onChange={handleChange} placeholder="Ex: Colégio Rosário" required />

        <div className="row">
          <div className="col">
            <label>Data (Ter-Qui) *</label>
            <input type="date" name="dataVisita" value={formData.dataVisita} onChange={handleChange} required />
          </div>
          <div className="col">
             <label>Horário *</label>
             <select name="horarioVisita" value={formData.horarioVisita} onChange={handleChange} required>
               <option value="">Selecione...</option>
               <option value="Manhã (09h - 11h)">Manhã (09h - 11h)</option>
               <option value="Tarde (14h - 16h)">Tarde (14h - 16h)</option>
               <option value="Outro (Noturno)">Outro / Noturno (Justificar)</option>
             </select>
          </div>
        </div>

        <label>Tipo de Grupo e Quantidade</label>
        <div className="radio-group">
          <label>
            <input type="radio" name="tipoGrupo" value="adultos" checked={formData.tipoGrupo === 'adultos'} onChange={handleChange} />
            Adultos (Máx 20)
          </label>
          <label>
            <input type="radio" name="tipoGrupo" value="criancas" checked={formData.tipoGrupo === 'criancas'} onChange={handleChange} />
            Crianças (Máx 15)
          </label>
        </div>
        <input type="number" name="qtdPessoas" value={formData.qtdPessoas} onChange={handleChange} min="1" max="30" />

        <label>Observações / Justificativa (Opcional)</label>
        <textarea name="mensagem" value={formData.mensagem} onChange={handleChange} rows={4} placeholder="Caso tenha selecionado horário noturno ou tenha necessidades especiais, descreva aqui." />

        <button type="submit" disabled={loading || !!erro}>
          {loading ? 'Enviando...' : 'Solicitar Agendamento'}
        </button>
      </form>
    </div>
  );
}

export default App;
