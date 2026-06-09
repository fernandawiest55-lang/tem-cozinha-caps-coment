
 document.addEventListener('DOMContentLoaded', () => {
      document.getElementById('recuperarForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value.trim();
        document.getElementById('emailError').textContent = '';
        if (!Validate.email(email)) {
          document.getElementById('emailError').textContent = 'Informe um e-mail válido.';
          return;
        }
        setLoading('btnRecuperar', 'btnSpinner', 'btnText', true);
        try {
          await apiFetch('/usuarios/recuperar-senha', { method: 'POST', body: JSON.stringify({ email }) });
          document.getElementById('recuperarForm').style.display = 'none';
          document.getElementById('successBox').style.display = 'block';
        } catch (err) {
          showAlert(err.message || 'Erro ao enviar. Tente novamente.');
        } finally {
          setLoading('btnRecuperar', 'btnSpinner', 'btnText', false);
        }
      });
    });



