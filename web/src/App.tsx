import { useState } from 'react';
import './App.css'; 

interface FormularioVisita {
  nomeVisitante: string;
  emailVisitante: string;
  mensagem: string;
}

function App() {
  const [form, setForm] = useState<FormularioVisita>({
    nomeVisitante: '',
    emailVisitante: '',
    mensagem: ''
  });

  const [status, setStatus] = useState<string>('');
  const [tipoStatus, setTipoStatus] = useState<'sucesso' | 'erro' | ''>('');

  const aoDigitar = (evento: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setForm({
      ...form,
      [evento.target.name]: evento.target.value
    });
  };

  const aoEnviar = async (evento: React.FormEvent) => {
    evento.preventDefault();
    setStatus('Enviando...');
    setTipoStatus('');

    try {
      const resposta = await fetch('http://localhost:8080/email/solicitar-visita', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });

      if (resposta.ok) {
        setStatus('✅ Sucesso! E-mail enviado.');
        setTipoStatus('sucesso');
        setForm({ nomeVisitante: '', emailVisitante: '', mensagem: '' });
      } else {
        setStatus('❌ Erro no envio.');
        setTipoStatus('erro');
      }
    } catch (erro) {
      console.error(erro);
      setStatus('❌ Erro de conexão com o servidor.');
      setTipoStatus('erro');
    }
  };

  return (
    <div className="container">
      <h2 className="titulo">Agendar Visita 🌱</h2>
      
      <form onSubmit={aoEnviar} className="formulario">
        
        <label>Seu Nome:</label>
        <input 
          className="campo"
          type="text" 
          name="nomeVisitante" 
          value={form.nomeVisitante} 
          onChange={aoDigitar} 
          placeholder="Ex: João da Silva"
          required
        />

        <label>Seu E-mail:</label>
        <input 
          className="campo"
          type="email" 
          name="emailVisitante" 
          value={form.emailVisitante} 
          onChange={aoDigitar} 
          placeholder="Ex: joao@email.com"
          required
        />

        <label>Mensagem:</label>
        <textarea 
          className="campo"
          name="mensagem" 
          value={form.mensagem} 
          onChange={aoDigitar} 
          placeholder="Gostaria de conhecer o projeto..."
          rows={4}
          required
        />

        <button type="submit" className="btn-enviar">
          Enviar Solicitação
        </button>

      </form>

      {/* Renderização Condicional com classe dinâmica */}
      {status && (
        <div className={`status ${tipoStatus}`}>
          {status}
        </div>
      )}
    </div>
  );
}

export default App;
