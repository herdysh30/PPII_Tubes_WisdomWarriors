package controller;

import com.lowagie.text.DocumentException;
import model.OTP;
import model.OTPMapper;
import org.apache.ibatis.session.SqlSession;
import view.OTPListView;

import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class OTPListController {
    private OTPListView view;
    private OTPMapper otpMapper;
    private SqlSession session;

    public OTPListController(OTPListView view, OTPMapper otpMapper, SqlSession session) {
        this.view = view;
        this.otpMapper = otpMapper;
        this.session = session;

        // Inisialisasi tabel atau listener
        loadOTPData();

        // Tambahkan listener ke tombol
        view.addRefreshButtonListener(new RefreshButtonListener());
        view.addBackButtonListener(new BackButtonListener());
        view.addDeleteButtonListener(new DeleteButtonListener());
        view.addExportButtonListener(new ExportButtonListener());
    }

    // Method untuk memuat data OTP ke JTable
    private void loadOTPData() {
        try {
            DefaultTableModel tableModel = (DefaultTableModel) view.getOTPTable().getModel();
            tableModel.setRowCount(0);

            List<OTP> otpList = otpMapper.getAllOTP(); 
            System.out.println("Data OTP: " + otpList); 
            if (otpList.isEmpty()) {
                System.out.println("Tidak ada data OTP.");
            }

            for (OTP otp : otpList) {
                Object[] rowData = {
                    otp.getOtpId(),
                    otp.getKodeOtp(),
                    otp.getCreatedAt(),
                    otp.getExpiresAt(),
                    otp.getStatus()
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Listener untuk tombol Refresh
    class RefreshButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadOTPData(); 
        }
    }

    // Listener untuk tombol Kembali
    class BackButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.dispose(); 
        }
    }
    class DeleteButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = view.getOTPTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Pilih baris yang ingin dihapus.");
                return;
            }

            int otpId = (int) view.getOTPTable().getValueAt(selectedRow, 0); 
            int confirm = JOptionPane.showConfirmDialog(view, "Yakin ingin menghapus OTP ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    otpMapper.deleteOTP(otpId);
                    session.commit();
                    JOptionPane.showMessageDialog(view, "OTP berhasil dihapus.");
                    loadOTPData(); // Refresh tabel setelah delete
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Gagal menghapus OTP: " + ex.getMessage());
                }
            }
        }
    }
    class ExportButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            // Dapatkan data dari tabel
            List<String[]> tableData = new ArrayList<>();
            for (OTP otp : otpMapper.getAllOTP()) {
                tableData.add(new String[]{
                        String.valueOf(otp.getOtpId()),
                        otp.getKodeOtp(),
                        otp.getCreatedAt() != null ? otp.getCreatedAt().toString() : "-",
                        otp.getExpiresAt() != null ? otp.getExpiresAt().toString() : "-",
                        otp.getStatus()
                });
            }

            // Header kolom
            String[] headers = {"ID OTP", "Kode OTP", "Dibuat Pada", "Berakhir Pada", "Status"};

            // File output
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan PDF");
            if (fileChooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath() + ".pdf";
                PDFExporter.exportTableToPDF(filePath,"Daftar OTP", tableData, headers);
                JOptionPane.showMessageDialog(view, "File berhasil diekspor ke " + filePath);
            }
        } catch (DocumentException | IOException ex) {
            JOptionPane.showMessageDialog(view, "Gagal mengekspor data: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}


}
